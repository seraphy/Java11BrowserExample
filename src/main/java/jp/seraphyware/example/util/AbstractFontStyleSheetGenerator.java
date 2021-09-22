package jp.seraphyware.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * フォントのスタイルシートを生成する.<br>
 * Sceneに対して生成したスタイルシートを適用する実装は派生クラスで行う.<br>
 */
public abstract class AbstractFontStyleSheetGenerator {

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

	/**
	 * シーンに対してフォントのルート設定されたCSSを追加適用します.<br>
	 * (すでに適用されている場合は呼び出し元で削除する必要があります。)<br>
	 * @param scene
	 * @param font
	 */
	public void applyStyleSheet(Scene scene, Font font) {
		URL cssPath = getCssURL();
		if (generatedCssFont == null || !generatedCssFont.equals(font)) {
			generatedCss = generateCss(font);
			putContents(cssPath, generatedCss);
			generatedCssFont = font;
		}

		if (generatedCss != null && generatedCss.length() > 0) {
			scene.getStylesheets().add(cssPath.toExternalForm());
		}
	}
	
	/**
	 * CSSを格納するURLを取得する。
	 * @return
	 */
	protected abstract URL getCssURL();
	
	/**
	 * 指定したURLにCSSテキストを設定する
	 * @param url
	 * @param contents
	 */
	protected abstract void putContents(URL url, String contents);
	
	/**
	 * CSSテンプレートを読む
	 * @return
	 * @throws IOException
	 */
	protected InputStream getInputCssTemplate() throws IOException {
		return AbstractWindowController.class.getResourceAsStream(DEFAULT_CSS_TEMPLATE);
	}

	public static class FontInfo {

		private String family;

		private double size;

		private FontPosture posture = FontPosture.REGULAR;

		private FontWeight weight = FontWeight.NORMAL;

		public String getFamily() {
			return family;
		}

		public void setFamily(String family) {
			this.family = family;
		}

		public double getSize() {
			return size;
		}

		public void setSize(double size) {
			this.size = size;
		}

		public FontPosture getPosture() {
			return posture;
		}

		public void setPosture(FontPosture posture) {
			this.posture = posture;
		}

		public FontWeight getWeight() {
			return weight;
		}

		public void setWeight(FontWeight weight) {
			this.weight = weight;
		}

		@Override
		public String toString() {
			return "FontInfo [family=" + family + ", size=" + size + ", posture=" + posture + ", weight=" + weight
					+ "]";
		}

		public Font getFont() {
			return Font.font(family, weight, posture, size);
		}

		public static FontInfo parse(Font font) {
			FontPosture selPosture = FontPosture.REGULAR;
			FontWeight selWeight = FontWeight.NORMAL;
		    for (String styleWord : font.getStyle().split("\\s")) {
		        FontPosture posture = FontPosture.findByName(styleWord);
		        if (posture != null) {
		        	selPosture = posture;
		        }

		        FontWeight weight = FontWeight.findByName(styleWord);
		        if (weight != null) {
		        	selWeight = weight;
		        }
		    }

		    String family = font.getFamily();
		    double size = font.getSize();

		    FontInfo info = new FontInfo();
		    info.setFamily(family);
		    info.setSize(size);
		    info.setPosture(selPosture);
		    info.setWeight(selWeight);

		    return info;
		}
	}

	/**
	 * フォントを指定して、対応するCSSテンプレートから適用済みCSSを生成する
	 * @param font フォント
	 * @return 生成されたCSS
	 */
	private String generateCss(Font font) {
		if (font == null) {
			return null;
		}

		FontInfo fontInfo = FontInfo.parse(font);

		Map<String, String> varMap = new HashMap<>();
	    varMap.put("font-size", Double.toString(fontInfo.getSize()));
		varMap.put("font-name", font.getName());
		varMap.put("font-family", fontInfo.getFamily());
		varMap.put("font-weight", Integer.toString(fontInfo.getWeight().getWeight()));
		varMap.put("font-style", fontInfo.getPosture() == FontPosture.ITALIC ? "italic" : "normal");

		try {
			InputStream inp = getInputCssTemplate();
			if (inp != null) {
				StringWriter wr = new StringWriter();
				try (InputStreamReader rd = new InputStreamReader(inp, StandardCharsets.UTF_8)) {
					expand(rd, wr, name -> {
						return varMap.computeIfAbsent(name, k -> {
							return System.getProperty(k);
						});
					});
				}

				return wr.toString();
			}
			return null;

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * 変数${xxx}を展開する
	 * @param rd
	 * @param wr
	 * @param resolver
	 * @throws IOException
	 */
	private void expand(Reader rd, Writer wr, Function<String, String> resolver) throws IOException {
		int ch;
		int mode = 0;
		StringBuilder nameBuf = new StringBuilder();
		while ((ch = rd.read()) != -1) {
			if (mode == 0) {
				if (ch == '$') {
					mode = 1;
				} else {
					wr.write(ch);
				}
			} else if (mode == 1) {
				if (ch == '{') {
					mode = 2;
				} else {
					wr.write('$');
					wr.write(ch);
					mode = 0;
				}
			} else if (mode == 2) {
				if (ch == '}') {
					String val = resolver.apply(nameBuf.toString());
					if (val != null) {
						wr.write(val);
					}
					mode = 0;
					nameBuf.setLength(0);
				} else {
					nameBuf.append((char) ch);
				}
			}
		}
	}
}
