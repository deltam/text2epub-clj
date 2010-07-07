; make epub metadata

(ns text2epub.epub
  (:use [clojure.contrib.duck-streams :only (reader writer file-str)]
        [hiccup.core]))


(defn mimetype []
  (with-open [w (writer "mimetype")]
    (.write w "application/epub+zip")))


(defn make-meta-inf []
  (let [dir (file-str "META-INF")]
    (if (not (. dir exists))
      (. dir mkdirs))
    (with-open [w (writer "META-INF/container.xml")]
      (.write w
              (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                   (html
                    [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                     [:rootfiles
                      [:rootfile {:full-path "content.opf" :media-type "application/oebps-package+xml"}]]]))))))


(defn out-content-opf [title id section_name]
  (with-open [w (writer "content.opf")]
    (.write w
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
                    [:item {:id section_name :href section_name :media-type "application/xhtml+xml"}]]
                   [:spine {:toc "ncx"}
                    [:itemref {:idref section_name}]]])))))


(defn out-ncx [id section_names]
  (with-open [w (writer "toc.ncx")]
    (.write w
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
                    (for [sec section_names]
                      [:navPoint {:id sec :playOrder "1"}
                       [:navLabel
                        [:text sec]]
                       [:content {:src sec}]])]])))))


(defn to-epub-text [filename text_name]
  (with-open [r (reader filename) w (writer text_name)]
    (.write w
            (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                 "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
                 (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
                        [:head
                         [:title filename]
                         [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
                        [:body
                         (for [line (line-seq r)]
                           [:p line])]])))))