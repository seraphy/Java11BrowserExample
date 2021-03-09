# Java11+OpenJFX+WebView実装例

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

https.protocolを指定しているのは、java11のbugでブラウザでhttps通信中に「No PSK Available」というエラーがコンソールに出るため。(Java13以降なら出ないらしい。)


## SEE ALSO
以前試したプロジェクト

https://github.com/seraphy/JavaFX11ModuleExample

