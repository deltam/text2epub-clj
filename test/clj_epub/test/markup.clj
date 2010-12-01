(ns clj-epub.test.markup
  (:use [clj-epub.markup] :reload-all)
  (:use [clojure.test]))


(deftest test-normalize-text
  (is (= "<p>test</p>test" (normalize-text "test\ntest")))
  (is (= "<br/>" (normalize-text "<br>")))
  (is (= "<img src=\"test\"/>" (normalize-text "<img src=\"test\">"))))

(deftest test-text->xhtml
  (is (= (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
              "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>"
              "<title>title</title>"
              "<meta content=\"application/xhtml+xml; charset=utf-8\" http-equiv=\"Content-Type\" /></head>"
              "<body><p><b>title</b></p>body</body></html>")
         (text->xhtml "title" "body"))))

(deftest test-files->epub-texts
  (is false))


(deftest test-cut-by-chapter-easy-markup
  (is (= '({:title "test" :text "test body"})
         (cut-by-chapter {:markup :easy-markup :title "" :text "!!test\ntest body"}))))
  (is (= '({:title "test1" :text "body1\nbody2"} {:title "test2" :text "body3\nbody4"})
         (cut-by-chapter {:markup :easy-markup :title "" :text "!!test1\nbody1\nbody2\n!!test2\nbody3\nbody4"})))

(deftest test-markup-text-easy-markup
  (is (= '({:ncx "title1" :text "text1\n"} {:ncx "title2" :text "text2"})
         (markup-text {:markup :easy-markup :title "title" :text "<h1>title1</h1>text1\n<h2>title2</h2>text2"}))))

(deftest test-cut-by-chapter-plain
  (is (= '({:title "test" :text "test body"})
         (cut-by-chapter {:markup :plain :title "test" :text "test body"}))))

(deftest test-markup-text-plain
  (is (= '({:title "test" :text "<pre>&lt;test body&gt;</pre>"})
         (markup-text {:markup :plain :title "test" :text "<test body>"}))))

(deftest test-cut-by-chapter-markdown
  (is false))

(deftest test-markup-text-markdown
  (is false))

(deftest test-markdown->html ; todo write more
  (is (= "<h1>test</h1>\n" (markdown->html "# test\n"))))

