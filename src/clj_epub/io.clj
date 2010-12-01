(ns clj-epub.io
  "input and output EPUB files"
  (:use [clj-epub epub zipf])
  (:import [java.io ByteArrayOutputStream]))


(defn- write-epub
  "write EPUB on zip file"
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (key epub)))
  (doseq [t (:html epub)]
    (deflated zos t))
  (.flush zos))


(defn epub->file
  "output EPUB file from apply EPUB info"
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

