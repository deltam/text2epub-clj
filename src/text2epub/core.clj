(ns text2epub.core
  "convert plain texts to ePub"
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub io]))

(defn show-help []
     (println "Usage: java -jar text2epub-clj-*-standalone.jar \"epub title\" <textfiles>.."))

; main
; usage: CMD epub-title <textfile>..
(defn -main
  ([option & args]
     (if (nil? option)
       (show-help)
       ; if-let? 
       (condp = option
         "-md" (let [[title files] args]; todo multi file
                 (if (nil? files)
                   (show-help)
                   (let [output (str (replace-re #"\..+$" "" files) ".epub")
                         epub-info {:output output :title title :input files}]
                     (info->epub epub-info))))
         "-pt" (println "plain text mode")
         "" (show-help))))
  ([]
     (show-help)))