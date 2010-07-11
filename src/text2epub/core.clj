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
        html_name "section01.xhtml"
        id (str (. java.util.UUID randomUUID))]
    (mimetype)
    (make-meta-inf)
    (out-content-opf epub_title id html_name)
    (out-ncx id [html_name])
    (to-epub-text text_name html_name)
    (to-zip epub_name
            ["META-INF/container.xml" "content.opf" "toc.ncx"]
            ["section01.xhtml"])))