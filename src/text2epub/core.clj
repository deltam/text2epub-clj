(ns text2epub.core
  "convert plain texts to ePub"
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub epub io]))


(defn show-help []
     (println "Usage: java -jar text2epub-clj-*-standalone.jar [options] \"epub title\" <textfiles>..\n"
              "options: -md  markdown\n"
              "         -pt  plain text\n"
              "         -df  default\n"))


; main
; usage: CMD epub-title <textfile>..
(defn -main
  ([] (show-help))
  ([option & args]
     (if (nil? option)
       (show-help)
       ; if-let?
       (let [marktype (condp = option
                        "-md" :markdown
                        "-pt" :plain
                        "-df" :default)]
         (prn marktype)
         (let [title (first args)
               files (drop 1 args)]; todo multi file
           (prn files)
           (if (or (nil? marktype) (nil? files))
             (show-help)
             (let [output (str (replace-re #"\..+$" "" (first files)) ".epub")
                   info {:output output :title title :input files :markup marktype}
                   epub (text->epub info)]
               (epub->file epub output))))))))