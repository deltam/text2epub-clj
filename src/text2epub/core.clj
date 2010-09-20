(ns text2epub.core
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub epub zipf]))

; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [[title input] args]
    (if (nil? input)
      (println "Usage: java -jar text2epub-clj-*-standalone.jar output.epub \"epub title\" <textfiles>..")
      (let [output (str (replace-re #"\..+$" "" input) ".epub")
            epub (text->epub {:output output :title title :input input})]
        (with-open [zos (open-zip output)]
          (stored zos (:mimetype epub))
          (doseq [key [:meta-inf :content-opf :toc-ncx]]
            (stored zos (get epub key)))
          (doseq [t (:html epub)]
            (stored zos t)))))))
