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

(deftest test-text->epub
  (is false))