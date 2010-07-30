(ns text2epub.core
  (:gen-class)
  (:use [text2epub epub]
        [hiccup.core]))


; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [epub-name  (first args)
        epub-title (second args)
        texts      (drop 2 args)]
    (gen-epub epub-name epub-title texts)))