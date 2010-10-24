(ns clj-epub.test.epub
  (:use [clj-epub.epub] :reload-all)
  (:use [clojure.test]))


(deftest test-mimetype
  (is (false? (nil? mimetype)))
  (is (= "mimetype" (:name (mimetype))))
  (is (= "application/epub+zip" (:text (mimetype)))))

(deftest test-meta-inf
  (is (false? (nil? meta-inf)))
  (is (= "META-INF/container.xml" (:name (meta-inf))))
;  (is (= "" (:text (meta-inf))))) ;todo
  )

(deftest test-content-opf
  (is (false? (nil? (content-opf "test" "test" "test" ["test"]))))
  (is (= "OEBPS/content.opf" (:name (content-opf "test" "test" "test" ["test"]))))
;  (is (= "" (:text (content-opf "test" "test" "test" ["test"])))))
  )

(deftest test-toc-ncx
  (is (= "OEBPS/toc.ncx" (:name (toc-ncx "test" ["test"]))))
)


(deftest test-slice-easy-text
  (is (= (list {:ncx "test" :text "test body"}) (slice-easy-text "" "!!test\ntest body")))
  (is (= (list {:ncx "test1" :text "body1\nbody2"}
               {:ncx "test2" :text "body3\nbody4"})
         (slice-easy-text "" "!!test1\nbody1\nbody2\n!!test2\nbody3\nbody4"))))

(deftest test-normalize-text
  (is (= "<p>test</p>test" (normalize-text "test\ntest")))
  (is (= "<br/>" (normalize-text "<br>")))
  (is (= "<img src=\"test\"/>" (normalize-text "<img src=\"test\">"))))

(deftest test-markdown->html ; todo write more
  (is (= "<h1>test</h1>\n" (markdown->html "# test\n"))))

(deftest test-slice-html
  (is (= "