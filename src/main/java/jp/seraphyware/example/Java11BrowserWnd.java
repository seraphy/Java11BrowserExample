package jp.seraphyware.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

/**
 * WebViewをコンテンツとして持つウィンドウクラス
 */
public class Java11BrowserWnd implements Initializable {

	/**
	 * ウィンドウの開閉回数を通知するためのインターフェイス
	 */
	public interface WindowReferenceCounter {

		/**
		 * ウィンドウが開いた場合
		 */
		void addRef();

		/**
		 * ウィンドウが閉じた場合
		 */
		void release();
	}


	/**
	 * ウィンドウ
	 */
	private Stage stg;

	@FXML
	private TextField txtUrl;

	@FXML
	private WebView webview;

	private WebEngine engine;

	private Stage owner;

	private final WindowReferenceCounter refCounter;

	public Java11BrowserWnd(Stage owner, WindowReferenceCounter refCounter) {
		this.owner = owner;
		this.refCounter = Objects.requireNonNull(refCounter);

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
		stg.setTitle(getClass().getSimpleName() + "@" + getImplementationVersion());
		stg.setScene(new Scene(parent));
		stg.setOnCloseRequest(evt -> onClose());
		stg.showingProperty().addListener((self, old, showing) -> {
			if (showing) {
				refCounter.addRef();
			} else {
				refCounter.release();
			}
		});
	}

	/**
	 * 指定したクラスを保持しているMETA-INF/MANIFEST.MF情報を取得する。
	 * (jarまたはfileのいずれの場所にあっても取得可能である。)
	 * @param cls クラス
	 * @return 魔にフェススト
	 * @throws IOException
	 */
	private static Manifest loadManifest(Class<?> cls) throws IOException {
		// このクラスを格納しているjarの中のMANIFESTファイルを読み取る
		URL res = cls.getResource(cls.getSimpleName() + ".class");
		String s = res.toString();
		res = new URL(s.substring(0, s.length() - (cls.getName() + ".class").length()) + "META-INF/MANIFEST.MF");
		System.out.println("MANIFEST-URL=" + res);

		Manifest mf = new Manifest();
		try (InputStream is = res.openStream()) {
			mf.read(is);
		}

		Attributes attrs = mf.getMainAttributes();
		for (Map.Entry<Object, Object> entry : attrs.entrySet()) {
			System.out.println(entry);
		}

		return mf;
	}

	private String getImplementationVersion() {
		try {
			Manifest mf = loadManifest(getClass());

			// マニフェストの実装バージョンの取得
			Attributes attrs = mf.getMainAttributes();
			return attrs.getValue("Implementation-Version");

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return ex.toString();
		}
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
			// メニューバーがある場合は独立ウィンドウ、無い場合は子ウィンドウとする
			Java11BrowserWnd child = new Java11BrowserWnd(popupFeature.hasMenu() ? null : stg, refCounter);
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
