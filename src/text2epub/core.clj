(ns text2epub.core
  "convert plain texts to ePub"
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub io]))

; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [[title input] args]
    (if (nil? input)
      (println "Usage: java -jar text2epub-clj-*-standalone.jar output.epub \"epub title\" <textfiles>..")
      (let [output (str (replace-re #"\..+$" "" input) ".epub")
            epub_info {:output output :title title :input input}]
        (output epub_info)))))