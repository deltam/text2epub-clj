(ns text2epub.core
  (:gen-class)
  (:use [clj-epub epub]))

; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [[epub-name epub-title & texts] args]
    (if (nil? epub-name)
      (println "Usage: java -jar text2epub-clj-*-standalone.jar output.epub \"epub title\" <textfiles>..")
      (gen-epub epub-name epub-title texts))))