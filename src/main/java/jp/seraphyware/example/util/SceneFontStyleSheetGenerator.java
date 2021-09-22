package jp.seraphyware.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javafx.scene.Scene;
import javafx.scene.text.Font;

/**
 * Sceneに適用されるフォントのスタイルシートを生成する.<br>
 * 生成されたスタイルシートはdataプロトコルのURLとして設定されるので、
 * 予め、{@link DataSchemeURLStreamHandlerFactory}をURLのストリームハンドラファクトリとして設定しておく必要がある。<br>
 * @see DataSchemeURLStreamHandlerFactory
 */
public class SceneFontStyleSheetGenerator extends AbstractFontStyleSheetGenerator {

	/**
	 * デフォルトのスタイルシートのテンプレートの格納先
	 */
	private static final String DEFAULT_CSS_TEMPLATE = "/styles.css.tmpl";

	/**
	 * CSSの生成に使ったフォント、まだ生成されていなればnull
	 */
	private Font generatedCssFont;

	/**
	 * 生成されたCSS、まだ生成されていなければnull
	 */
	private String generatedCss;

	@Override
	protected InputStream getInputCssTemplate() throws IOException {
		return AbstractWindowController.class.getResourceAsStream(DEFAULT_CSS_TEMPLATE);
	}
	
	/**
	 * シーンに対してフォントのルート設定されたCSSを追加適用します.<br>
	 * (すでに適用されている場合は呼び出し元で削除する必要があります。)<br>
	 * @param scene
	 * @param font
	 */
	public void applyStyleSheet(Scene scene, Font font) {
		if (generatedCssFont == null || !generatedCssFont.equals(font)) {
			generatedCss = generateCss(font);
			generatedCssFont = font;
		}

		if (generatedCss != null && generatedCss.length() > 0) {
			String base64 = new String(Base64.getEncoder().encode(generatedCss.getBytes(StandardCharsets.UTF_8)));
//			String encoded = URLEncoder.encode(generatedCss, StandardCharsets.UTF_8)
//					.replace("+", "%20")
//					.replace("*", "%2a")
//					.replace("-", "%2d")
//					.replace("_", "%5f");
			scene.getStylesheets().add("data:text/css;base64," + base64);
		}
	}
}
