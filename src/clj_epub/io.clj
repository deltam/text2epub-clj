(ns clj-epub.io
  "input and output ePub files"
  (:use [clojure.contrib.string :only (replace-re)]
        [clj-epub epub zipf])
  (:import [java.io ByteArrayOutputStream]))


(defn- write-epub
  ""
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (key epub)))
  (doseq [t (:html epub)]
    (deflated zos t))
  (.flush zos))


(defn epub->file
  "output ePub file from apply info"
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos epub)))


(defn epub->byte
  "output EPUB bytes"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))

