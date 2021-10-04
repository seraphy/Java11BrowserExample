package jp.seraphyware.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;

/**
 * WenEngineに適用されるフォントのスタイルシートを生成する.<br>
 * 生成されたスタイルシートはdataプロトコルのURLとして設定されるので、
 * 予め、{@link DataSchemeURLStreamHandlerFactory}をURLのストリームハンドラファクトリとして設定しておく必要がある。<br>
 * @see DataSchemeURLStreamHandlerFactory
 */
public class WebFontStyleSheetGenerator extends FontStyleSheetGenerator {

	/**
	 * デフォルトのスタイルシートのテンプレートの格納先
	 */
	private static final String DEFAULT_CSS_TEMPLATE = "/web-styles.css.tmpl";

	/**
	 * CSSの生成に使ったフォント、まだ生成されていなればnull
	 */
	private Font generatedCssFont;

	/**
	 * 生成されたCSS、まだ生成されていなければnull
	 */
	private String generatedCss;

	protected InputStream getInputCssTemplate() throws IOException {
		return AbstractWindowController.class.getResourceAsStream(DEFAULT_CSS_TEMPLATE);
	}
	
	/**
	 * WebEngineに対してフォントのルート設定されたCSSを追加適用します.<br>
	 * (すでに適用されている場合は呼び出し元で削除する必要があります。)<br>
	 * @param engine
	 * @param font
	 */
	public void applyStyleSheet(WebEngine engine, Font font) {
		if (generatedCssFont == null || !generatedCssFont.equals(font)) {
			try {
				generatedCss = generateCss(font, getInputCssTemplate());
				generatedCssFont = font;

			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

		if (generatedCss != null && generatedCss.length() > 0) {
			String base64 = new String(Base64.getEncoder().encode(generatedCss.getBytes(StandardCharsets.UTF_8)));
			engine.setUserStyleSheetLocation("data:text/css;base64," + base64);
		}
	}
}
