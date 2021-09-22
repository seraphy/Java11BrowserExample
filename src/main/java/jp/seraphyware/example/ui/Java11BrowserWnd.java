package jp.seraphyware.example.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jp.seraphyware.example.util.AbstractWindowController;
import jp.seraphyware.example.util.CDIFXMLLoader;
import jp.seraphyware.example.util.ErrorDialogUtils;
import jp.seraphyware.example.util.ManifestHelper;
import jp.seraphyware.example.util.MessageResource;

/**
 * WebViewをコンテンツとして持つウィンドウクラス
 */
@Dependent
public class Java11BrowserWnd extends AbstractWindowController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Java11BrowserWnd.class);

	@FXML
	private TextField txtUrl;

	@FXML
	private WebView webview;

	private WebEngine engine;

	@Inject
	@CDIFXMLLoader
	private Instance<FXMLLoader> ldrProvider;
	
	@Inject
	@MessageResource
	private ResourceBundle resources;

	private final ObjectProperty<Function<Java11BrowserWnd, Java11BrowserWnd>> createChildCallback = new SimpleObjectProperty<>();

	public ObjectProperty<Function<Java11BrowserWnd, Java11BrowserWnd>> createChildCallback() {
		return createChildCallback;
	}

	@Override
	protected void makeRoot() {
		FXMLLoader ldr = ldrProvider.get();
		try {
			ldr.setController(this);
			ldr.setLocation(getClass().getResource("Java11BrowserWnd.fxml")); //$NON-NLS-1$
			setRoot(ldr.load());

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} finally {
			ldrProvider.destroy(ldr);
		}
	}

	@Override
	protected Stage createStage() {
		Stage stg = super.createStage();
		stg.initModality(Modality.WINDOW_MODAL);
		String implementsVersion = ManifestHelper.getImplementationVersion();
		String moduleName = getClass().getModule().getName();
		String title = resources.getString("window.title") + "@" + moduleName + "/" + implementsVersion; //$NON-NLS-1$
		stg.setTitle(title);
		return stg;
	}

	@Override
	public void onCloseRequest(WindowEvent event) {
		onClose();
	}
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		engine = webview.getEngine();
		engine.setOnAlert(webevent -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(getStage());
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("INFORMATION");
			alert.setHeaderText(webevent.getData());
			alert.showAndWait();
		});

		engine.setOnError(error -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(getStage());
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("ERROR");
			alert.setHeaderText(error.getMessage());
			alert.showAndWait();
		});

		engine.setCreatePopupHandler(popupFeature -> {
			// メニューバーがある場合は独立ウィンドウ、無い場合は子ウィンドウとする
			Java11BrowserWnd parent = popupFeature.hasMenu() ? null : this;
			Java11BrowserWnd newWnd = createChildCallback.get().apply(parent);
			newWnd.openWindow();
			return newWnd.engine;
		});

		engine.locationProperty().addListener((self, old, value) -> {
			txtUrl.setText(value);
		});
	}

	public void load(String url) {
		logger.info("url=" + url);
		txtUrl.setText(url);
		engine.load(url);
	}

	@FXML
	protected void onClose() {
		closeWindow();
	}

	@FXML
	protected void onBrowse() {
		String url = txtUrl.getText();
		engine.load(url);
	}
	
	@Inject
	private Instance<SysPropsWnd> sysPropsWndProv;
	
	@FXML
	protected void onShowPreferences() {
		SysPropsWnd wnd = sysPropsWndProv.get();
		try {
			wnd.setOwner(getStage());
			wnd.showAndWait();
		
		} catch (Exception ex) {
			ErrorDialogUtils.showException(getStage(), ex);

		} finally {
			sysPropsWndProv.destroy(wnd);
		}
	}
	
	@FXML
	protected void onFontSetting() {
		
	}
}
