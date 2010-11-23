(ns clj-epub.markup
  "make EPUB text from some markuped text"
  (:use [hiccup.core :only (html escape-html)])
  (:import [com.petebevin.markdown MarkdownProcessor]))


; 章立ての切り分け
(defmulti cut-by-chapter :markup)
;  各記法による修飾
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
  {:ncx  title
   :src  (str title ".html")
   :name (str "OEBPS/" title ".html")
   :text (text->xhtml title text)})


(defn get-epub-meta
  "EPUBメタデータが埋めこまれている場合、それを返す"
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

;; 簡易記法タグ
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
  (let [text (escape-html (:text easy-type))
        html (str "<b>" (:title easy-type) "</b>"
                  (. text replaceAll "([^(<[^>]+>)\n]*)\n" "<p>$1</p>"))]
    {:title (:title easy-type), :text html}))



;;; プレインテキスト用
; プレインテキストをそのまま切り分けず返す
(defmethod cut-by-chapter :plain
  [plain-type]
  (list {:title (:title plain-type) :text (:text plain-type)}))

(defmethod markup-text :plain
  [plain-type]
  (let [escape-text (escape-html (:text plain-type))
        text (str "<pre>" escape-text "</pre>")]
    {:title (:title plain-type), :text text}))



;;; Markdown記法
;; ファイルを開いてePubのページごとに切り分ける(<h*>で切り分ける)
(defmethod cut-by-chapter :markdown
  [md-type]
  (let [html (:text md-type)
        prelude (re-find #"(?si)^(.*?)(?=(?:<h\d>|$))" html)
        sections (for [section (re-seq #"(?si)<h(\d)>(.*?)</h\1>(.*?)(?=(?:<h\d>|\s*$))" html)]
                   (let [[all level value text] section]
                     {:title value :text text}))]
      sections))

(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))

(defmethod markup-text :markdown
  [md-type]
  (markdown->html (:text md-type)))