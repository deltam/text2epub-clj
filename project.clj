(defproject text2epub-clj "0.0.3-alpha"
  :description "convert plain text into .epub"
  :url "http://github.com/deltam/text2epub-clj"
  :main text2epub.core
  :run-aliases {:convert [text2epub.core -main]}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [hiccup "0.2.6"]]
  :dev-dependencies [[lein-run "1.0.0-SNAPSHOT"]])
