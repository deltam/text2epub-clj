# text2epub-clj

Convert plain text into .epub, wriiten on Clojure.

プレインテキストをePubに変換するツールをClojureで書いてみた。


ePub created by this tool, checked open it by these ePub readers.

このツールで作成したepubは、これらのePubリーダーで開けることをチェックしています。

    iBooks 

    Stanza http://www.lexcycle.com/


## Usage

    $ lein deps
    $ lein uberjar
    $ java -jar text2epub-clj-standalone.jar [options] "epub_title" <text>..

This tool is able to bind some text file together into ePub.

このツールでは複数のテキストファイルをePubにまとめることが出来ます。


ePub's index page are viewed list of text file names.

ePubの目次ページにはテキストファイル名が表示されます


(CAUTION!: all text file should be encoded UTF-8.)

(注意!: すべてのテキストファイルはUTF-8エンコード必須です)


## Function
* convert plain text into ePub.
* binding some text files together into ePub
* ultra-simple markup for plain text and ePub
* <s>added "!!" on line head, split pages on ePub.(e.g. samples/hello.txt)</s>


## TODO

* writing unit test.
* added easy-markup to include image files.
* added easy-markup function (markdown or others)
* added EPUB metadata markup function.
** "!title!" tag -> EPUB title.
** "!author!" tag -> EPUB author.

## License

Copyright (c) 2010 deltam (deltam@gmail.com).

Licensed under the MIT License (http://www.opensource.org/licenses/mit-license.php)
