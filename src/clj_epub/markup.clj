(ns clj-epub.markup
  "make EPUB text from some markuped text"
  (:use [hiccup.core :only (html escape-html)]
        [hiccup.page-helpers :only (doctype xml-declaration)])
  (:import [java.net URLEncoder]
           [com.petebevin.markdown MarkdownProcessor]))


; 章立ての切り分け
(defmulti cut-by-chapter :markup)
; 各記法による修飾
(defmulti markup-text :markup)


(defn normalize-text
  "テキストからePub表示に不都合なHTMLタグ、改行を取り除く"
  [text]
  (.. text
      (replaceAll "<br>" "<br/>")
      (replaceAll "<img([^>]*)>" "<img$1/>")))


(defn text->xhtml
  "title,textをつなげたXHTMLを返す"
  [title text]
  (html
   (xml-declaration "UTF-8")
   (doctype :xhtml-transitional)
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:title title]
     [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
    [:body (normalize-text text)]]))


(defn epub-text
  "EPUBのページ構成要素を作成し、返す"
  [id title text]
  {:label title
   :ncx  id
   :src  (str id ".html")
   :name (str "OEBPS/" id ".html")
   :text (text->xhtml title text)})


(defn get-epub-meta
  "TODO: EPUBメタデータが埋めこまれている場合、それを返す"
  [markup-type text]
  {:title "" :author ""})


(defn files->epub-texts
  "ファイルの内容をEPUB用HTMLに変換して返す"
  [markup-type filenames]
  (let [snippets (flatten
                  (map #(cut-by-chapter {:markup markup-type :title % :text (slurp %)})
                       filenames))
        markups (flatten
                 (map #(markup-text {:markup markup-type :title (:title %) :text (:text %)})
                      snippets))]
    (map-indexed (fn [index chapter] (epub-text (str "chapter-" index) (:title chapter) (:text chapter)))
                   markups)))



;; EPUB簡易記法

; 簡易記法タグ
(def meta-tag
     {:chapter "[\\^\n]!!" ; !!
      :title   "!title!"})

; 簡単なマークアップで目次を切り分ける
(defmethod cut-by-chapter :easy-markup
  [{title :title text :text}]
  (let [sections (for [section (re-seq #"(?si)!!\s*(.*?)\n(.*?)(?=(?:!!|\s*$))" text)]
                   (let [[all value body] section]
                     {:title value :text all}))]
    sections))

;  (for [chap (. text split (:chapter meta-tag))]
;    (let [chap-title (.. chap (replaceAll "\n.*" "\n") trim)
;          chap-text  (. chap (replaceFirst "^[^\n]*\n" ""))]  ; cut ncx string
;      {:title chap-title, :text chap-text})))

(defmethod markup-text :easy-markup
  [{title :title text :text}]
  (let [html (str "<b>" title "</b>"
                  (. text replaceAll "([^(<[^>]+>)\n]*)\n*" "<p>$1</p>"))]
    {:title title, :text html}))



;; プレインテキスト用
; プレインテキストをそのまま切り分けず返す
(defmethod cut-by-chapter :plain
  [{title :title text :text}]
  (list {:title title, :text text}))

(defmethod markup-text :plain
  [{title :title text :text}]
  {:title title, :text (str "<pre>" (escape-html text) "</pre>")})



;; Markdown記法
; ファイルを開いて見出しごとに章を切り分ける
(defmethod cut-by-chapter :markdown
  [{title :title text :text}]
  (let [sections (for [section (re-seq #"(?si)#+\s*(.*?)\n(.*?)(?=(?:#+|\s*$))" text)]
                   (let [[all value body] section]
                     {:title value :text all}))]
    sections))

(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))

(defmethod markup-text :markdown
  [{title :title text :text}]
  {:title title, :text (markdown->html text)})