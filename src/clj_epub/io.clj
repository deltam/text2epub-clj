(ns clj-epub.io
  "input and output ePub files"
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub epub zipf]))

(defn info->epub
  "output ePub file from apply info"
  [info]
  (let [epub (text->epub info)]
    (with-open [zos (open-zip (info :output))]
      (stored zos (:mimetype epub))
      (doseq [key [:meta-inf :content-opf :toc-ncx]]
        (stored zos (get epub key)))
      (doseq [t (:html epub)]
        (stored zos t)))))
