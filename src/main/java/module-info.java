module java11browser {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;

    requires org.slf4j;

    // FXMLLoaderがリフレクションを使うためにopensする必要がある
    opens jp.seraphyware.example;
    exports jp.seraphyware.example;
}
