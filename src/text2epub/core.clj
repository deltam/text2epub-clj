(ns text2epub.core
  (:gen-class)
  (:use [text2epub epub]
        [hiccup.core]))


; main
; usage: CMD output.epub epub_title <textfile>..
(defn -main [& args]
  (let [temp       (take 2 args)
        epub-name  (first temp)
        epub-title (second temp)
        texts      (drop 2 args)]
    (gen-epub epub-name epub-title texts)))