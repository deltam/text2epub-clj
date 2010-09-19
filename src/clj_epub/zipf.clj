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

;;;
; str base

(defn stored [#^ZipOutputStream zos {name :name text :text}]
  (.setMethod zos ZipOutputStream/STORED)
  (let [crc   (CRC32.)
        ze    (ZipEntry. name)
        bytes  (.getBytes text)
        count (alength bytes)]
    (.update crc bytes)
    (doto ze
      (.setSize count)
      (.setCrc (.getValue crc)))
    (doto zos
      (.putNextEntry ze)
      (.write bytes 0 count)
      (.closeEntry))))

(defn- output-stream [input #^ZipOutputStream output]
  (let [buf (char-array 1024)]
    (loop [count (.read input buf 0 1024)]
      (if (not= count -1)
        (let [str (String. buf 0 count)
              bytes (.getBytes str "UTF-8")
              len (alength bytes)]
          (.write output bytes 0 len))
        (recur (.read input buf 0 1024))))))

(defn deflated [#^ZipOutputStream zos {name :name text :text}]
  (.setMethod zos ZipOutputStream/DEFLATED)
  (. zos putNextEntry (ZipEntry. name))
  (let [fis (InputStreamReader. (ByteArrayInputStream. (.getBytes text "UTF-8")) "UTF-8")]
    (output-stream fis zos)
    (.closeEntry zos)))

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



(defn make-zipf
  "generate zip-file. args are collection of filenames."
  [f storef deflatedf textf]
  (with-open [zos (open-zip f)]
    ; 非圧縮
    (doseq [f storef]
      (add-store-file zos f))
    ; 圧縮
    (doseq [f deflatedf]
      (add-deflated-file zos f))
    (doseq [f textf]
      (add-text-file zos f))))
