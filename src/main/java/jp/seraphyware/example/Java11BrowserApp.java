package jp.seraphyware.example;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jp.seraphyware.example.Java11BrowserWnd.WindowReferenceCounter;

public class Java11BrowserApp extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Java11BrowserApp.class);

	public void init() {
		Platform.setImplicitExit(false);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		logger.info("**START**");

		AtomicInteger refCount = new AtomicInteger();
		Java11BrowserWnd wnd = new Java11BrowserWnd(null, new WindowReferenceCounter() {

			@Override
			public void addRef() {
				int count = refCount.incrementAndGet();
				logger.info("refCount=" + count);
			}

			@Override
			public void release() {
				int count = refCount.decrementAndGet();
				logger.info("refCount=" + count);
				if (count == 0) {
					// 開いているウィンドウが無くなったら閉じる
					Platform.exit();
				}
			}
		});

		String url = getClass().getResource("index.html").toString();
		wnd.load(url);

		wnd.show();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		logger.info("**DONE**");
		System.exit(0);
	}
}
