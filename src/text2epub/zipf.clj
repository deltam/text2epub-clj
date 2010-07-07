; zip file output

(ns text2epub.zipf
  (:use [text2epub epub])
  (:import [java.util.zip ZipEntry ZipOutputStream CRC32]
           [java.io InputStreamReader FileOutputStream FileInputStream]))


(defn open-zip [f]
  (ZipOutputStream. (FileOutputStream. f)))


(defn add-store-file [#^ZipOutputStream zos f]
  (. zos setMethod ZipOutputStream/STORED)
  (let [fis (FileInputStream. f)
        ze (ZipEntry. f)
        crc (CRC32.)
        buf (byte-array 256)
        count (. fis read buf 0 256)]
    (. ze setSize count)
    (. crc update buf)
    (. ze setCrc (. crc getValue))
    (. zos putNextEntry ze)
    (. zos write buf)
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
    (loop [count (.fis read buf 0 1024)]
      (if (not (= count -1))
        (let [s (String. buf 0 count)
              b (. s getBytes "UTF-8")
              len (alength b)]
          (. zos write b 0 len)))
      (if (= count -1)
        nil
        (recur (. fis read buf 0 1024))))
    (. zos closeEntry)))

  
(defn to-zip [f]
  (let [zos (ZipOutputStream. (FileOutputStream. f))
        metaf ["META-INF/container.xml" "content.opf" "toc.ncx"]
        secf  ["section01.xhtml"]]
    ; 非圧縮
;    (add-store-file zos "mimetype")
    (. zos setMethod ZipOutputStream/STORED)
    (let [data (. "application/epub+zip" getBytes)
          ze (ZipEntry. "mimetype")
          crc (CRC32.)]
      (. ze setSize (. "application/epub+zip" length))
      (. crc update data)
      (. ze setCrc (. crc getValue))
      (. zos putNextEntry ze)
      (. zos write data)
      (. zos closeEntry))
   ; 圧縮
    (. zos setMethod ZipOutputStream/DEFLATED)
    ; metadata
    (doseq [f metaf]
      (. zos putNextEntry (ZipEntry. f))
      (let [fis (FileInputStream. f)
            buf (byte-array 1024)]
        (loop [count (. fis read buf 0 1024)]
          (if (not (= count -1))
            (. zos write buf 0 count))
          (if (= count -1)
            nil
            (recur (. fis read buf 0 1024)))))
      (. zos closeEntry))
    ; text data
    (doseq [f secf]
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
              (recur (. fis read buf 0 1024)))))
      (. zos closeEntry))
    (. zos close)))