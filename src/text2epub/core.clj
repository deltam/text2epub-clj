(ns text2epub.core
  "convert plain texts to EPUB"
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clojure.contrib.command-line :only (with-command-line)]
        [clj-epub epub io])
  (:import [java.lang Exception]
           [java.io File]))


; main
(defn -main
  ([] (-main "--help"))
  ([& args]
     (with-command-line args
       "text2epub-clj -- Convert plain text to EPUB
Usage: java -jar text2epub-standalone.jar [-df|-pt|-md] [-t \"epub title\"] <textfiles>.."
       [[default?  df?  "easy-markup text" true]
        [plain?    pt?  "plain text"]
        [markdown? md?  "markdown format text"]
        [title t        "EPUB title" "Untitled"]
        filenames]
       (let [marktype (cond
                       plain?    :plain
                       markdown? :markdown
                       default?  :easy-markup
                       :else     :easy-markup)]
         (let [name   (.getName (File. (first filenames)))
               output (str (replace-re #"\..+$" "" name) ".epub") ; output at current directory
               info   {:title title :input filenames :markup marktype}
               epub   (text->epub info)]
           (epub->file epub output)
           ;run report
           (println "input:  " filenames)
           (println "title:  " title)
           (println "output: " output))))))
