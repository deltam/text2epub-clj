; zip file output
(ns clj-epub.zipf
  (:import [java.util.zip ZipEntry ZipOutputStream CRC32]
           [java.io InputStreamReader
                    ByteArrayInputStream
                    FileOutputStream
                    FileInputStream]))


(defn open-zip
  "open ZipOutputStream" 
  [f]
  (ZipOutputStream. (FileOutputStream. f)))

(defn store-str [#^ZipOutputStream zos ftext]
  (. zos setMethod ZipOutputStream/STORED)
  (let [ze    (ZipEntry. (ftext :name))
        crc   (CRC32.)
        text  (. (ftext :text) getBytes)
        count (alength text)]
    (. ze setSize count)
    (. crc update text)
    (. ze setCrc (. crc getValue))
    (. zos putNextEntry ze)
    (. zos write text 0 count)
    (. zos closeEntry)))


(defn deflated-str [#^ZipOutputStream zos ftext]
  (. zos setMethod ZipOutputStream/DEFLATED)
  (. zos putNextEntry (ZipEntry. (ftext :name)))
  (let [fis (InputStreamReader. (ByteArrayInputStream. (. (ftext :text) getBytes "UTF-8")) "UTF-8")
        buf (char-array 1024)]
    (loop [count (. fis read buf 0 1024)]
      (if (not (= count -1))
        (let [s (String. buf 0 count)
              b (. s getBytes "UTF-8")
              len (alength b)]
          (. zos write b 0 len)))
      (if (= count -1)
        nil
        (recur (. fis read buf 0 1024))))
    (. zos closeEntry)))


;;;;
;; file base

(defn add-store-file [#^ZipOutputStream zos f]
  (. zos setMethod ZipOutputStream/STORED)
  (let [fis (FileInputStream. f)
        ze (ZipEntry. f)
        crc (CRC32.)
        buf (byte-array 20)
        count (. fis read buf 0 20)]
    (. ze setSize count)
    (. crc update buf)
    (. ze setCrc (. crc getValue))
    (. zos putNextEntry ze)
    (. zos write buf 0 count)
    (. zos closeEntry)))


(defn add-deflated-file [#^ZipOutputStream zos f]
    (. zos setMethod ZipOutputStream/DEFLATED)
    (. zos putNextEntry (ZipEntry. f))
    (let [fis (FileInputStream. f)
          buf (byte-array 1024)]
      (loop [count (. fis read buf 0 1024)]
        (if (not (= count -1))
          (. zos write buf 0 count))
        (if (= count -1)
          nil
          (recur (. fis read buf 0 1024))))
      (. zos closeEntry)))


(defn add-text-file [#^ZipOutputStream zos f]
  (. zos putNextEntry (ZipEntry. f))
  (let [fis (InputStreamReader. (FileInputStream. f) "UTF-8")
        buf (char-array 1024)]
    (loop [count (. fis read buf 0 1024)]
      (if (not (= count -1))
        (let [s (String. buf 0 count)
              b (. s getBytes "UTF-8")
              len (alength b)]
          (. zos write b 0 len)))
      (if (= count -1)
        nil
        (recur (. fis read buf 0 1024))))
    (. zos closeEntry)))


  
(defn make-zipf [f storef deflatedf textf]
  (with-open [zos (open-zip f)]
    ; 非圧縮
    (doseq [f storef]
      (add-store-file zos f))
    ; 圧縮
    (doseq [f deflatedf]
      (add-deflated-file zos f))
    (doseq [f textf]
      (add-text-file zos f))))
