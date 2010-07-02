(ns text2epub.core
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (reader writer file-str)]
        [hiccup.core])
  (:import [java.util.zip ZipEntry ZipOutputStream CRC32]
           [java.io InputStreamReader FileOutputStream FileInputStream]))


      
(def mimetype "application/epub+zip")


(defn make-meta-inf []
  (let [dir (file-str "META-INF")]
    (if (not (. dir exists))
      (. dir mkdirs))
    (with-open [w (writer "META-INF/container.xml")]
      (binding [*out* w]
        (print
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                [:rootfiles
                 [:rootfile {:full-path "content.opf" :media-type "application/oebps-package+xml"}]]])))))))


(defn out-content-opf [title id section_name]
  (with-open [w (writer "content.opf")]
    (binding [*out* w]
      (print
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
               [:itemref {:idref section_name}]]]))))))


(defn out-ncx [id section_names]
  (with-open [w (writer "toc.ncx")]
    (binding [*out* w]
      (print
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
                  [:content {:src sec}]])]]))))))


(defn to-epub-text [filename text_name]
  (with-open [r (reader filename) w (writer text_name)]
    (binding [*out* w]
      (println
       (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
            (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
                   [:head
                    [:title filename]
                    [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
                   [:body
                    (for [line (line-seq r)]
                      [:p line])]]))))))


(defn to-zip [epub_name]
  (let [zos (ZipOutputStream. (FileOutputStream. epub_name))
        metaf ["META-INF/container.xml" "content.opf" "toc.ncx"]
        secf  ["section01.xhtml"]]
    ; 非圧縮
    (. zos setMethod ZipOutputStream/STORED)
    (let [data (. mimetype getBytes)
          ze (ZipEntry. "mimetype")
          crc (CRC32.)]
      (. ze setSize (. mimetype length))
      (. crc update data)
      (. ze setCrc (. crc getValue))
      (. zos putNextEntry ze)
      (. zos write data)
      (. zos closeEntry))
    ; 圧縮
    (. zos setMethod ZipOutputStream/DEFLATED)
    ; metadata
    (doseq [f metaf]
      (. zos putNextEntry (ZipEntry. f))
      (let [fis (FileInputStream. f)
            buf (byte-array 1024)]
        (loop [count (. fis read buf 0 1024)]
          (if (not (= count -1))
            (. zos write buf 0 count))
          (if (= count -1)
            nil
            (recur (. fis read buf 0 1024)))))
      (. zos closeEntry))
    ; text data
    (doseq [f secf]
      (. zos putNextEntry (ZipEntry. f))
      (let [fis (InputStreamReader. (FileInputStream. f) "UTF-8")
            buf (char-array 1024)]
         (loop [count (. fis read buf 0 1024)]
            (if (not (= count -1))
              (let [s (String. buf 0 count)
                    b (. s getBytes "UTF-8")
                    len (alength b)]
                (. zos write b 0 len)))
            (if (= count -1)
              nil
              (recur (. fis read buf 0 1024)))))
      (. zos closeEntry))
    (. zos close)))


; main
; usage: CMD text.txt text.epub epub_title
(defn -main [& args]
  (let [files (take 3 args)
        text_name (first files)
        epub_name (second files)
        epub_title (last files)
        html_name "section01.xhtml"
        id (str (rand-int 999999999))]
    (println args)
    (make-meta-inf)
    (out-content-opf epub_title id html_name)
    (out-ncx id [html_name])
    (to-epub-text text_name html_name)
    (to-zip epub_name)))

  
