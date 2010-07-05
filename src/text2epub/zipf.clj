(ns text2epub.zipf
  (:use [text2epub epub])
  (:import [java.util.zip ZipEntry ZipOutputStream CRC32]
           [java.io InputStreamReader FileOutputStream FileInputStream]))

      
(defn to-zip [epub_name]
  (let [zos (ZipOutputStream. (FileOutputStream. epub_name))
        metaf ["META-INF/container.xml" "content.opf" "toc.ncx"]
        secf  ["section01.xhtml"]]
    ; 非圧縮
    (. zos setMethod ZipOutputStream/STORED)
    (let [data (. mimetype getBytes)
          ze (ZipEntry. "mimetype")
          crc (CRC32.)]
      (. ze setSize (. mimetype length))
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