; make epub metadata
(ns clj-epub.epub
  (:use [clj-epub zipf]
        [clojure.contrib.duck-streams :only (reader writer file-str)]
        [hiccup.core]))


(defn- file-write [f str]
  (with-open [w (writer f)]
    (.write w str)))

(defn- ftext [name text]
  {:name name :text text})


(defn mimetype []
  (ftext "mimetype"
         "application/epub+zip"))


(defn make-meta-inf []
  (ftext "META-INF/container.xml"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                [:rootfiles
                 [:rootfile {:full-path "content.opf" :media-type "application/oebps-package+xml"}]]]))))


(defn out-content-opf [title id sections]
;  (println "opf")
;  (prn sections)
  (ftext "content.opf"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:package {:xmlns "http://www.idpf.org/2007/opf"
                          :unique-identifier "BookID"
                          :version "2.0"}
                [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"
                            :xmlns:opf "http://www.idpf.org/2007/opf"}
                 [:dc:title title]
                 [:dc:language "ja"]
                 [:dc:creator "nobody"]
                 [:dc:identifier {:id "BookID"} id]]
                [:manifest
                 [:item {:id "ncx" :href "toc.ncx" :media-type "application/x-dtbncx+xml"}]
                 (for [s sections]
                   [:item {:id s :href (str s ".html") :media-type "application/xhtml+xml"}])]
                [:spine {:toc "ncx"}
                 (for [s sections]
                   [:itemref {:idref s}])]]))))


(defn out-ncx [id section_titles]
  (ftext "toc.ncx"
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
                   [:navPoint {:id sec :playOrder (str (count (take-while #(not (= sec %)) section_titles)))}; todo
                    [:navLabel
                     [:text sec]]
                    [:content {:src (str sec ".html")}]])
                 ]]))))

(def meta-tag
     {:chapter "!!"
      :title   "!title!"})

(defn pre-text [filename]
  "簡単なマークアップで目次を切り分ける"
  (with-open [r (reader filename)]
    (let [text (reduce str (for [line (line-seq r)] (str line "\n")))]
      (for [sec (.split text (meta-tag :chapter))]
        (let [ncx  (.. sec (replaceAll "\n.*" "\n") trim)
              text (.. sec (replaceFirst "^[^\n]*\n" ""))]
          {:ncx ncx, :text text})))))
;    (for [line (line-seq r)]
;      (if (re-matches (tag-regex :chapter) line)
;        ((println "chapter: " line))))))
         

(defn str-to-epub-text [text_str text_name]
    (let [filename (str text_name ".html")
          valid-text (.. text_str
                         (replaceAll "([^\n]*)\n" "<p>$1</p>")
                         (replaceAll "<br>" "<br/>")
                         (replaceAll "<img([^>]*)>" "<img$1/>"))]
      (ftext filename
             (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
                (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
                       [:head
                        [:title text_name]
                        [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
                       [:body
                        (str "<p><b>" text_name "</b></p>"
                             valid-text)
                        ]])))))


(defn to-epub-text [filename text_name]
  (with-open [r (reader filename)]
    (let [text (reduce str (for [line (line-seq r)] line))]
      (str-to-epub-text text text_name))))


(defn gen-epub
  "generate ePub file. args are epub filename, epub title of metadata, includes text files."
  [epub-name epub-title text-files]
  (let [id       (str (. java.util.UUID randomUUID))
        ptexts   (flatten (map #(pre-text %) text-files))
;        ppp      (prn (map first ptexts))
        sections (seq (map #(get % :ncx) ptexts))
;        kkk      (prn sections)
;        htmls    (map #(str % ".html") sections)
        epubinf  {:mimetype (mimetype)
                  :metainf  (make-meta-inf)
                  :opf      (out-content-opf epub-title id sections)
                  :ncx      (out-ncx id sections)
                  :texts    (for [s ptexts]
                              (str-to-epub-text (s :text) (s :ncx)))}]
    (with-open [zos (open-zip epub-name)]
      (store-str zos (epubinf :mimetype))
      (doseq [key [:metainf :opf :ncx]]
        (deflated-str zos (epubinf key)))
      (doseq [t (epubinf :texts)]
        (deflated-str zos t)))))
