package jp.seraphyware.example;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Java11BrowserWnd implements Initializable {

	private Stage stg;

	@FXML
	private TextField txtUrl;

	@FXML
	private WebView webview;

	private WebEngine engine;

	private Stage owner;

	public Java11BrowserWnd(Stage owner) {
		this.owner = owner;

		FXMLLoader ldr = new FXMLLoader();
		ldr.setController(this);
		ldr.setLocation(getClass().getResource("Java11BrowserWnd.fxml"));

		Parent parent;
		try {
			parent = ldr.load();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		stg = new Stage();
		if (owner != null) {
			stg.initOwner(owner);
		}
		stg.setTitle(getClass().getSimpleName());
		stg.setScene(new Scene(parent));
		stg.setOnCloseRequest(evt -> onClose());
	}

	public Stage getOwner() {
		return owner;
	}

	public Stage getStage() {
		return stg;
	}

	public void show() {
		stg.show();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		engine = webview.getEngine();
		engine.setOnAlert(webevent -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(stg);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("INFORMATION");
			alert.setHeaderText(webevent.getData());
			alert.showAndWait();
		});

		engine.setOnError(error -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(stg);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("ERROR");
			alert.setHeaderText(error.getMessage());
			alert.showAndWait();
		});

		engine.setCreatePopupHandler(popupFeature -> {
			Java11BrowserWnd child = new Java11BrowserWnd(stg);
			child.show();
			return child.engine;
		});

		engine.locationProperty().addListener((self, old, value) -> {
			txtUrl.setText(value);
		});
	}

	public void load(String url) {
		System.out.println("url=" + url);
		txtUrl.setText(url);
		engine.load(url);
	}

	@FXML
	protected void onClose() {
		stg.close();
	}

	@FXML
	protected void onBrowse() {
		String url = txtUrl.getText();
		engine.load(url);
	}
}