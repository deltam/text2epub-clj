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
       (let [marktype (condp = option
                        "-md" :markdown
                        "-pt" :plain
                        "-df" :default)]
         (prn marktype)
         (let [[title files] args]; todo multi file
           (if (or (nil? marktype) (nil? files))
             (show-help)
             (let [output (str (replace-re #"\..+$" "" files) ".epub")
                   epub-info {:output output :title title :input files :markup marktype}]
               (info->epub epub-info)))))))
  ([]
     (show-help)))