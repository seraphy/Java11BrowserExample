# Java11+OpenJFX+WebView実装例 [![Build Status](https://travis-ci.com/seraphy/Java11BrowserExample.svg?branch=master)](https://travis-ci.com/seraphy/Java11BrowserExample)

## これはなにか？

OpenJDKのJava11とOpenJFX15を用いて、WebViewを使ったアプリケーションをモジュールとして実行する実験例である。

モジュールとして配布物を作成する。また、``jlink`` によるモジュールを含むJREの最小ランタイムを生成する。


## ビルド方法

openjdk11をmavenの実行環境にした上で、

```shell
mvn package exec:exec
```

で、ビルドとモジュールによる実行ができる。

## 実行方法

``target/mods``下にモジュールのjarが生成される。

また、本プロジェクトのモジュール名、バージョンとメインクラスもjarに設定済みである。

> バージョン3.1.2以降、JARファイルにmodule-info.classが含まれている場合、このプラグインは追加の属性でモジュラー記述子（module-info.class）を更新します

このmodsに対して以下のようにモジュール名を指定することで起動できる。

```shell
java11 -p target/mods -m java11browser -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
```

あるいは、依存jarのClass-Pathもマニフェストに追加しているため、クラスパス形式での実行可能jarとしても実行できる。

```shell
java11 -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 -jar target/mods/java11browser-0.0.1-SNAPSHOT.jar
```


https.protocolを指定しているのは、java11のbugでブラウザでhttps通信中に「No PSK Available」というエラーがコンソールに出るため。(Java13以降なら出ないらしい。)

## ランタイムの作成

依存するモジュールが分かっている場合、以下のように``jlink``を使うことで、JREと依存モジュールを合わせたランタイムを生成できる。

(依存モジュールは1つのmodulesファイルに統合される。)

```shell
jlink --module-path target/mods --add-modules java.base,javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.web,java11browser --launcer java11browser=java11browser --output jre_min
```

これで作成されたランタイムは、以下のように起動できる。

```shell
jre_min/bin/java -m java11browser
```

もしくはランチャとなるモジュール起動のシェルスクリプトが生成されているため、コマンドでも起動できる。

```shell
jre_min/bin/java11browser
```

依存しているモジュールを表示するためには、``jdeps`` を用いる。

```shell
$ jdeps --module-path target/mods --list-deps -recursive target/mods/java11browser-0.0.1-SNAPSHOT.jar
   java.base
   javafx.base
   javafx.controls
   javafx.fxml
   javafx.graphics
   javafx.web
```

※ mavevのlinkプラグインを使うと、openjfxが展開するxxxEmptyという中身が空の自動モジュール名のjarが依存関係として含まれてしまいエラーとなる。
現時点で回避方法はなさそうなので、antで明示的にjlinkを起動させている。

## SEE ALSO

以前試したMaven + Java11 + OpenJFX の実験プロジェクト

https://github.com/seraphy/JavaFX11ModuleExample

