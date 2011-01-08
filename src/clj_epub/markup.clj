(ns clj-epub.markup
  "make EPUB text from some markuped text"
  (:use [hiccup.core :only (html escape-html)])
  (:import [java.net URLEncoder]
           [com.petebevin.markdown MarkdownProcessor]))


;TODO Markdown記法、日本語セクション名でエラーあり。調査中
(defn- url-encode [s]
;  (URLEncoder/encode s))
  s)


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
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
       "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
       (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
              [:head
               [:title title]
               [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
              [:body (normalize-text text)]])))


(defn epub-text
  "ePubのページ構成要素を作成し、返す"
  [title text]
  {:label title
   :ncx  (url-encode title)
   :src  (str (url-encode title) ".html")
   :name (str "OEBPS/" (url-encode title) ".html")
   :text (text->xhtml title text)})


(defn get-epub-meta
  "TODO: EPUBメタデータが埋めこまれている場合、それを返す"
  [markup-type text]
  {:title "" :author ""})


(defn files->epub-texts
  "ファイルの内容をEPUB用HTMLに変換して返す"
  [markup-type filenames]
  (let [chapters (flatten
                  (map #(cut-by-chapter {:markup markup-type :title % :text (slurp %)})
                       filenames))
        markups (flatten
                 (map #(markup-text {:markup markup-type :title (:title %) :text (:text %)})
                      chapters))]
    (for [t markups]
      (epub-text (:title t) (:text t)))))



;; EPUB簡易記法

; 簡易記法タグ
(def meta-tag
     {:chapter "[\\^\n]!!" ; !!
      :title   "!title!"})

; 簡単なマークアップで目次を切り分ける
(defmethod cut-by-chapter :easy-markup
  [easy-type]
  (for [chap (. (:text easy-type) split (:chapter meta-tag))]
    (let [sec chap
          title (.. sec (replaceAll "\n.*" "\n") trim)
          text  (. sec (replaceFirst "^[^\n]*\n" ""))]  ; cut ncx string
      {:title title, :text text})))

(defmethod markup-text :easy-markup
  [easy-type]
  (let [title (:title easy-type)
        html (str "<b>" title "</b>"
                  (. (:text easy-type) replaceAll "([^(<[^>]+>)\n]*)\n*" "<p>$1</p>"))]
    {:title title, :text html}))



;; プレインテキスト用
; プレインテキストをそのまま切り分けず返す
(defmethod cut-by-chapter :plain
  [plain-type]
  (list {:title (:title plain-type) :text (:text plain-type)}))

(defmethod markup-text :plain
  [plain-type]
  (let [escape-text (escape-html (:text plain-type))
        text (str "<pre>" escape-text "</pre>")]
    {:title (:title plain-type), :text text}))



;; Markdown記法
; ファイルを開いて見出しごとに章を切り分ける
(defmethod cut-by-chapter :markdown
  [md-type]
  (let [text (:text md-type)
        sections (for [section (re-seq #"(?si)#+\s*(.*?)\n(.*?)(?=(?:#+|\s*$))" text)]
                   (let [[all value body] section]
                     {:title value :text all}))]
    sections))


(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))

(defmethod markup-text :markdown
  [md-type]
  {:title (:title md-type) :text (markdown->html (:text md-type))})