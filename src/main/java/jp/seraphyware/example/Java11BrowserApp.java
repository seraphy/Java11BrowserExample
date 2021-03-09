package jp.seraphyware.example;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Java11BrowserApp extends Application implements Initializable {

	private Stage stg;
	
	@FXML
	private TextField txtUrl;
	
	@FXML
	private WebView webview;
	
	private WebEngine engine;
	
	private Path tempDir;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader ldr = new FXMLLoader();
		ldr.setController(this);
		ldr.setLocation(getClass().getResource("Java11BrowserApp.fxml"));
		Parent parent = ldr.load();
		stg = new Stage();
		stg.setTitle(getClass().getSimpleName());
		stg.setScene(new Scene(parent));
		stg.setOnCloseRequest(evt -> onClose());
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
		
		try {
			tempDir = Files.createTempDirectory("webview");
			System.out.println("tempDir=" + tempDir);
			engine.setUserDataDirectory(tempDir.toFile());

		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}

		String url = getClass().getResource("index.html").toString();
		System.out.println("url=" + url);
		engine.load(url);
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		
		if (tempDir != null) {
			try {
				deleteRecursive(tempDir);

			} catch (IOException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	private static void deleteRecursive(Path pathToBeDeleted) throws IOException {
		if (Files.exists(pathToBeDeleted)) {
			Files.walkFileTree(pathToBeDeleted, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}
	
	@FXML
	protected void onClose() {
		stg.close();
	}
	
	@FXML
	protected void onBrowse() {
		String url = txtUrl.getText();
		webview.getEngine().load(url);
	}
}
