package jp.seraphyware.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class Java11BrowserApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("**START**");
		Java11BrowserWnd wnd = new Java11BrowserWnd(null);

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
