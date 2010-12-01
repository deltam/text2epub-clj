(defproject text2epub-clj "0.0.4-alpha"
  :description "convert plain text into .epub"
  :url "http://github.com/deltam/text2epub-clj"
  :main text2epub.core
  :run-aliases {:convert [text2epub.core -main]}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [hiccup "0.2.6"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-run "1.0.0-SNAPSHOT"]]
  :repositories {"markdownj" "http://scala-tools.org/repo-releases"})
