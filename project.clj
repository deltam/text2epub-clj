(defproject text2epub-clj "0.0.4-alpha"
  :description "convert plain text into .epub"
  :url "http://github.com/deltam/text2epub-clj"
  :main text2epub.core
  :run-aliases {:convert text2epub.core}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [hiccup "0.2.6"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]]
  :dev-dependencies [[lein-clojars "0.5.0"]
                     [swank-clojure "1.2.1"]]
  :repositories {"markdownj" "http://scala-tools.org/repo-releases"}
  :uberjar-name "text2epub-standalone.jar")
