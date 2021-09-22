package jp.seraphyware.example.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jp.seraphyware.example.util.DataSchemeURLStreamHandlerFactory;
import jp.seraphyware.example.util.ErrorDialogUtils;
import jp.seraphyware.example.util.ManifestHelper;
import jp.seraphyware.example.util.MemorySchemeURLStreamHandlerFactory;
import jp.seraphyware.example.util.MemorySchemeURLStreamHandlerFactory.ContentsBody;

/**
 * JavaFXのライフサイクルを管理する。
 */
public class Java11BrowserApp extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Java11BrowserApp.class);

	/**
	 * WeldSE
	 */
	private Weld weld;
	
	/**
	 * CDIコンテナ.
	 */
	private WeldContainer weldContainer;

	/**
	 * 開いているウィンドウ数、0になった場合に自動的にアプリケーションを終了させるため。
	 */
	private final AtomicInteger activeWindowCount = new AtomicInteger();


	/**
	 * 初期化
	 */
	@Override
	public void init() {
		// メモリ上のURL「memoryプロトコル」を使えるようにする
		URL.setURLStreamHandlerFactory(
				new DataSchemeURLStreamHandlerFactory(MemorySchemeURLStreamHandlerFactory.getInstance()));
		
		// 動的にシステム情報を表示するメモリプロトコル(診断・実験用)
		try {
			MemorySchemeURLStreamHandlerFactory.getInstance().putContents(new URI("memory:manifest"),
					new ContentsBody() {
						@Override
						public byte[] getBytes() {
							return ManifestHelper.toString(ManifestHelper.loadManifest(Java11BrowserApp.class))
									.getBytes(StandardCharsets.UTF_8);
						}

						@Override
						public String getContentType() {
							return "text/plain";
						}
					});

		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}

		// CDI準備
		weld = new Weld();
	}

	/**
	 * 開始
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		assert Platform.isFxApplicationThread();
		logger.info("**START**");
		try {
			// JavaFXスレッドで補足されていない例外が到達したらエラー表示する
			Thread.currentThread().setUncaughtExceptionHandler((t, ex) -> {
				logger.error("An uncaughted exception was occured.", ex);
				ErrorDialogUtils.showException(null, ex);
			});

			// CDIコンテナ起動
			weldContainer = weld.initialize();

			// 最初のウィンドウをオープンする
			Java11BrowserWnd wnd = createWindow(null);
			String url = getClass().getResource("/html/index.html").toString();
			wnd.load(url);

			wnd.openWindow();

			// ウィンドウの起動に成功したあとは、明示的な終了によってのみ閉じるようにする。
			Platform.setImplicitExit(false);

		} catch (Throwable ex) {
			logger.error("failed to start", ex);
			throw ex;
		}
	}

	/**
	 * 新しいウィンドウを作成する。
	 * @param parent 親となるウインドウ、ない場合はnull可
	 * @return 新しいウインドウ
	 */
	private Java11BrowserWnd createWindow(Java11BrowserWnd parent) {
		Instance<Java11BrowserWnd> wndFactory = CDI.current().select(Java11BrowserWnd.class);
		// WeldInstance<Java11BrowserWnd> wndFactory = weldContainer.select(Java11BrowserWnd.class); weldを直接利用しても良い
		Java11BrowserWnd wnd = wndFactory.get();
		if (parent != null) {
			wnd.setOwner(parent.getStage());
		}
		wnd.createChildCallback().set(this::createWindow);

		Stage stg = wnd.getStage();
		stg.showingProperty().addListener((self, old, showing) -> {
			if (showing) {
				int count = activeWindowCount.incrementAndGet();
				logger.info("activeWindowCount={}", count);

			} else {
				int count = activeWindowCount.decrementAndGet();
				logger.info("activeWindowCount={}", count);
				wndFactory.destroy(wnd);
				if (count == 0) {
					// 開いているウィンドウが無くなったら終了する
					Platform.exit();
				}
			}
		});

		return wnd;
	}

	/**
	 * 停止する。
	 */
	@Override
	public void stop() throws Exception {
		super.stop();
		// CDIを停止する。
		weldContainer.shutdown();
		logger.info("**DONE**");
		System.exit(0);
	}
}
