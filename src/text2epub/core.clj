(ns text2epub.core
  (:gen-class)
  (:use [text2epub epub]
        [hiccup.core]))


; main
; usage: CMD text.txt text.epub epub_title
(defn -main [& args]
  (let [files (take 3 args)
        text_name (first files)
        epub_name (second files)
        epub_title (last files)]
    (gen-epub epub_name epub_title [text_name])))