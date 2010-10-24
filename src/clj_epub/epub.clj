(ns clj-epub.epub
  "make epub metadata"
  (:use [clojure.contrib.io :only (reader)]
        [hiccup.core])
  (:import [java.util UUID]
           [com.petebevin.markdown MarkdownProcessor]))


(defn- generate-uuid
  "generate uuid for ePub dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(defn- ftext [name text]
  "binding name and text"
  {:name name :text text})


(defn mimetype
  "body of mimetype file for ePub format"
  []
  (ftext "mimetype"
         "application/epub+zip"))


(defn meta-inf
  "container.xml for ePub format"
  []
  (ftext "META-INF/container.xml"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                [:rootfiles
                 [:rootfile {:full-path "OEBPS/content.opf" :media-type "application/oebps-package+xml"}]]]))))


(defn content-opf
  "content body & metadata(author, id, ...) on ePub format"
  [title author id sections]
  (ftext "OEBPS/content.opf"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:package {:xmlns "http://www.idpf.org/2007/opf"
                          :unique-identifier "BookID"
                          :version "2.0"}
                [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"
                            :xmlns:opf "http://www.idpf.org/2007/opf"}
                 [:dc:title title]
                 [:dc:language "ja"]
                 [:dc:creator author]
                 [:dc:identifier {:id "BookID"} id]]
                [:manifest
                 [:item {:id "ncx" :href "toc.ncx" :media-type "application/x-dtbncx+xml"}]
                 (for [s sections]
                   [:item {:id s :href (str s ".html") :media-type "application/xhtml+xml"}])]
                [:spine {:toc "ncx"}
                 (for [s sections]
                   [:itemref {:idref s}])]]))))


(defn toc-ncx
  "index infomation on ePub format"
  [id section_titles]
  (ftext "OEBPS/toc.ncx"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">"
              (html
               [:ncx {:version "2005-1" :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
                [:head
                 [:meta {:content id :name "dtb:uid"}]
                 [:meta {:content "0" :name "dtb:totalPageCount"}]
                 [:meta {:content "1" :name "dtb:depth"}]
                 [:meta {:content "0" :name "dtb:maxPageNumber"}]]
                [:navMap
                 (for [sec section_titles]
                   [:navPoint {:id sec :playOrder (str (inc (count (take-while #(not (= sec %)) section_titles))))}; todo
                    [:navLabel
                     [:text sec]]
                    [:content {:src (str sec ".html")}]])
                 ]]))))

(def meta-tag
     {:chapter "!!"
      :title   "!title!"})

(defn slice-easy-text
  "簡単なマークアップで目次を切り分ける"
  [_ text]
  (for [sec (.split text (meta-tag :chapter))]
    (let [ncx  (.. sec (replaceAll "\n.*" "\n") trim)
          text (.. sec (replaceFirst "^[^\n]*\n" ""))]
      {:ncx ncx, :text text})))


(defn normalize-text
  "テキストからePub表示に不都合なHTMLタグ、改行を取り除く"
  [text]
  (.. text
      (replaceAll "([^\n]*)\n" "<p>$1</p>")
      (replaceAll "<br>" "<br/>")
      (replaceAll "<img([^>]*)>" "<img$1/>")))


(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))


(defn slice-html
  "ファイルを開いてePubのページごとに切り分ける(<h*>で切り分ける)"
  [title html]
  (let [prelude (re-find #"(?si)^(.*?)(?=(?:<h\d>|$))" html)
        sections (for [section (re-seq #"(?si)<h(\d)>(.*?)</h\1>(.*?)(?=(?:<h\d>|\s*$))" html)]
                   (let [[all level value text] section]
                     {:ncx value :text text}))]
;    (if prelude
;      (cons {:ncx title :text (get prelude 1)} sections)
      sections))


(defn no-slice-text
  "プレインテキストをそのまま切り分けず返す "
  [title text]
  (list {:ncx title :text text}))

; 以下、マルチメソッドで切り替える
; 修飾記法の切り替え
(def markup-types
     {:markdown markdown->html
      :default  (fn [text] text)
      :plain    (fn [text] text)})

; ePub切り分け方法を切り替える
(def slice-types
     {:markdown slice-html
      :default  slice-easy-text
      :plain    no-slice-text})


(defn text->xhtml
  "title,textをつなげたXHTMLを返す"
  [title text]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
       "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
       (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
              [:head
               [:title title]
               [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
              [:body (str "<p><b>" title "</b></p>"
                          (normalize-text text))]
              ])))


(defn epub-text
  "ePubのページ構成要素を作成し、返す"
  [title text]
  (ftext (str "OEBPS/" title ".html")
         (text->xhtml title text)))


(defn text->epub
  "generate ePub file. args are epub filename, epub title of metadata, includes text files."
  [{output :output input-files :input title :title author :author marktype :markup}]
  (let [id       (generate-uuid)
        mfiles   (map #(ftext % ((markup-types marktype) (slurp %))) input-files) ; todo refactoring
        ptexts   (flatten (map #((slice-types marktype) (:name %) (:text %)) mfiles)) ; ePub page cut by files
        sections (map #(get % :ncx) ptexts)]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf title (or author "Nobody") id sections)
     :toc-ncx     (toc-ncx id sections)
     :html        (for [s ptexts]
                    (epub-text (s :ncx) (s :text)))}))