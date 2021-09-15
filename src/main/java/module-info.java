module java11browser {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;

    requires org.slf4j;
    
	requires transitive org.controlsfx.controls;
	requires weld.se.shaded;

    // FXMLLoaderがリフレクションを使うためにopensする必要がある
    opens jp.seraphyware.example;
    opens jp.seraphyware.example.ui;
    opens jp.seraphyware.example.util;

    exports jp.seraphyware.example;
    exports jp.seraphyware.example.ui;
    exports jp.seraphyware.example.util;
}
