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
  (is (= '({:ncx "test" :text "test body"})
         (slice-easy-text "" "!!test\ntest body")))
  (is (= '({:ncx "test1" :text "body1\nbody2"} {:ncx "test2" :text "body3\nbody4"})
         (slice-easy-text "" "!!test1\nbody1\nbody2\n!!test2\nbody3\nbody4"))))

(deftest test-normalize-text
  (is (= "<p>test</p>test" (normalize-text "test\ntest")))
  (is (= "<br/>" (normalize-text "<br>")))
  (is (= "<img src=\"test\"/>" (normalize-text "<img src=\"test\">"))))

(deftest test-markdown->html ; todo write more
  (is (= "<h1>test</h1>\n" (markdown->html "# test\n"))))

(deftest test-slice-html
  (is (= '({:ncx "title1" :text "text1\n"} {:ncx "title2" :text "text2"})
         (slice-html "title" "<h1>title1</h1>text1\n<h2>title2</h2>text2"))))

(deftest test-no-slice-text
  (is (= '({:ncx "title" :text "bodytext"}) (no-slice-text "title" "bodytext"))))

(deftest test-text->xhtml
  (is (= (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
              "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>"
              "<title>title</title>"
              "<meta content=\"application/xhtml+xml; charset=utf-8\" http-equiv=\"Content-Type\" /></head>"
              "<body><p><b>title</b></p>body</body></html>")
         (text->xhtml "title" "body"))))

;(deftest test-epub-text

;(deftest test-text-epub