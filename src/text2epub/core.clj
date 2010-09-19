(ns text2epub.core
  (:gen-class)
  (:use [clj-epub epub zipf]))

; main
; usage: CMD output.epub epub-title <textfile>..
(defn -main [& args]
  (let [[output title input] args]
    (if (nil? output)
      (println "Usage: java -jar text2epub-clj-*-standalone.jar output.epub \"epub title\" <textfiles>..")
      (let [epub (text->epub {:output output :title title :input input})]
        (with-open [zos (open-zip output)]
          (store-str zos (epub :mimetype))
          (doseq [key [:meta-inf :content-opf :toc-ncx]]
            (deflated-str zos (epub key)))
          (doseq [t (epub :texts)]
            (deflated-str zos t)))))))
