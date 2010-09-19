; make epub metadata
(ns clj-epub.epub
  (:use [clj-epub zipf]
        [clojure.contrib.duck-streams :only (reader writer file-str)]
        [hiccup.core])
  (:import [java.util UUID]))

(defn generate-uuid []
  (str (UUID/randomUUID)))

(defn- ftext [name text]
  {:name name :text text})

(defn mimetype []
  (ftext "mimetype"
         "application/epub+zip"))

(defn meta-inf []
  (ftext "META-INF/container.xml"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                [:rootfiles
                 [:rootfile {:full-path "content.opf" :media-type "application/oebps-package+xml"}]]]))))


(defn content-opf [title id sections]
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


(defn ncx [id section_titles]
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
    (let [text (apply str (for [line (line-seq r)] (str line "\n")))]
      (for [sec (.split text (meta-tag :chapter))]
        (let [ncx  (.. sec (replaceAll "\n.*" "\n") trim)
              text (.. sec (replaceFirst "^[^\n]*\n" ""))]
          {:ncx ncx, :text text})))))
;    (for [line (line-seq r)]
;      (if (re-matches (tag-regex :chapter) line)
;        ((println "chapter: " line))))))

(defn normalize-text [text]
  (.. text
      (replaceAll "([^\n]*)\n" "<p>$1</p>")
      (replaceAll "<br>" "<br/>")
      (replaceAll "<img([^>]*)>" "<img$1/>")))

(defn text->epub [title text]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
       "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
       (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
              [:head
               [:title title]
               [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
              [:body (str "<p><b>" title "</b></p>"
                          (normalize-text text))]
              ])))

(defn epub-text [title text]
  (ftext (str title ".html")
         (text->epub title text)))

(defn gen-epub
  "generate ePub file. args are epub filename, epub title of metadata, includes text files."
  [epub-name epub-title text-files]
  (let [id       (generate-uuid)
        ptexts   (first (map #(pre-text %) text-files))
;        ppp      (prn ptexts)
        sections (seq (map #(get % :ncx) ptexts))
;        kkk      (prn sections)
;        htmls    (map #(str % ".html") sections)
        epubinf  {:mimetype (mimetype)
                  :metainf  (meta-inf)
                  :opf      (content-opf epub-title id sections)
                  :ncx      (ncx id sections)
                  :texts    (for [s ptexts]
                              (epub-text (s :ncx) (s :text)))}]
    (with-open [zos (open-zip epub-name)]
      (store-str zos (epubinf :mimetype))
      (doseq [key [:metainf :opf :ncx]]
        (deflated-str zos (epubinf key)))
      (doseq [t (epubinf :texts)]
        (deflated-str zos t)))))
