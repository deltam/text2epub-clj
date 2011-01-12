# text2epub-clj

Convert plain text into .epub, wriiten on Clojure.

プレインテキストをEPUBに変換するツールをClojureで書いてみた。


EPUB file created by this tool, checked open it by these EPUB readers.

このツールで作成したEPUBファイルは、これらのEPUBリーダーで開けることをチェックしています。

    iBooks 

    Stanza http://www.lexcycle.com/


## Usage

    $ lein deps
    $ lein uberjar
    $ java -jar text2epub-clj-standalone.jar --help
    Usage: java -jar text2epub-standalone.jar [-df|-pt|-md] [-t "epub title"] <textfiles>..
    Options
      --default, --df    easy-markup text      [default true]
      --plain, --pt      plain text
      --markdown, --md   markdown format text
      --title, -t <arg>  EPUB title            [default Untitled]


This tool is able to bind some text file together into EPUB.

このツールでは複数のテキストファイルをEPUBにまとめることが出来ます。


EPUB's index page are viewed list of text file names.

EPUBの目次ページにはテキストファイル名が表示されます


(CAUTION!: all text file should be encoded UTF-8.)

(注意!: すべてのテキストファイルはUTF-8エンコード必須です)


## Function
* convert plain text into EPUB.
* binding some text files together into EPUB
* ultra-simple markup for plain text and EPUB
* <s>added "!!" on line head, split pages on EPUB.(e.g. samples/hello.txt)</s>


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
