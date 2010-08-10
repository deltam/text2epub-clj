(ns text2epub.core
  (:gen-class)
  (:use [clj-epub epub]))


; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [epub-name  (first args)
        epub-title (second args)
        texts      (drop 2 args)]
    (if (= 0 (count args))
      (println "Usage: java -jar text2epub-clj-*-standalone.jar output.epub \"epub title\" <textfiles>..")
      (gen-epub epub-name epub-title texts))))