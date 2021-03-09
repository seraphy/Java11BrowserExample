package jp.seraphyware.example;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jp.seraphyware.example.Java11BrowserWnd.WindowReferenceCounter;

public class Java11BrowserApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("**START**");

		AtomicInteger refCount = new AtomicInteger();
		Java11BrowserWnd wnd = new Java11BrowserWnd(null, new WindowReferenceCounter() {

			@Override
			public void addRef() {
				int count = refCount.incrementAndGet();
				System.out.println("refCount=" + count);
			}

			@Override
			public void release() {
				int count = refCount.decrementAndGet();
				System.out.println("refCount=" + count);
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
		System.out.println("**DONE**");
	}
}
