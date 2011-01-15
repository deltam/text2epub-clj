(ns text2epub.core
  "convert plain texts to EPUB"
  (:gen-class)
  (:use [clojure.contrib.string :only (replace-re)]
        [clojure.contrib.command-line :only (with-command-line)]
        [clj-epub epub io])
  (:import [java.lang Exception]
           [java.io File]))


(defn- convert [output epub-info]
  (let [epub (text->epub epub-info)]
    (epub->file epub output)
    (println "INPUT:  " (:input epub-info))
    (println "TITLE:  " (:title epub-info))
    (println "OUTPUT: " output)))

(defn- file-status [filenames]
  (reduce into (map #(hash-map % (.lastModified (File. %))) filenames)))

(defn- dateformat [datetime]
  (let [form (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")]
    (.format form datetime)))

(defn- standby-loop [output epub-info]
  (let [inputs (:input epub-info)
        last-status (atom (file-status inputs))]
    (println "standby-mode: exit on Ctrl-c")
    (convert output epub-info)
    (loop [now-status (file-status inputs)]
      (if (some #(> (now-status %) (@last-status %)) inputs)
        (do (println "Update EPUB: " (dateformat (java.util.Date.)))
            (convert output epub-info)
            (reset! last-status now-status)))
      (Thread/sleep 1000) ; interval
      (recur (file-status inputs)))))


(defn -main
  ([] (-main "--help"))
  ([& args]
     (with-command-line args
       "text2epub-clj -- Convert plain text to EPUB
Usage: java -jar text2epub-standalone.jar [-df|-pt|-md] [-t \"epub title\"] [-sb] <textfiles>.."
       [[default?  df?  "easy-markup text" true]
        [plain?    pt?  "plain text"]
        [markdown? md?  "markdown format text"]
        [title t        "EPUB title" "Untitled"]
        [standby?  sb?  "run as standby-mode(output EPUB repeatedly when modify input files)" false]
        filenames]
       (let [marktype (cond
                       plain?    :plain
                       markdown? :markdown
                       default?  :easy-markup)
             name   (.getName (File. (first filenames)))
             output (str (replace-re #"\..+$" "" name) ".epub") ; output at current directory
             info   {:title title :input filenames :markup marktype}]
         (if (not standby?)
           (convert output info)
           (standby-loop output info))))))