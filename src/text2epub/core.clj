(ns text2epub.core
  (:gen-class)
  (:use [text2epub epub zipf]
        [hiccup.core]))


; main
; usage: CMD text.txt text.epub epub_title
(defn -main [& args]
  (let [files (take 3 args)
        text_name (first files)
        epub_name (second files)
        epub_title (last files)
        sec_title ["section01"]
        html_name (map #(str % ".html") sec_title)
        id (str (. java.util.UUID randomUUID))]
    (mimetype)
    (make-meta-inf)
    (out-content-opf epub_title id html_name sec_title)
    (out-ncx id html_name)
    (to-epub-text text_name (first html_name))
    (to-zip epub_name
            ["META-INF/container.xml" "content.opf" "toc.ncx"]
            html_name)))