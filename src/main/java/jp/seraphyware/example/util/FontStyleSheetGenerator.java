package jp.seraphyware.example.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * フォントのスタイルシートを生成する.
 * 生成されたスタイルシートはメモリ上のmemory:カスタムスキームによってアクセスされるリソースとして保存される。
 * @see MemorySchemeURLStreamHandlerFactory
 */
public class FontStyleSheetGenerator extends AbstractFontStyleSheetGenerator {

	private static final String DEFAULT_CSS_NAME = "styles.css";

	@Override
	protected URL getCssURL() {
		try {
			return new URL("memory://./" + DEFAULT_CSS_NAME);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void putContents(URL url, String contents) {
		try {
			byte[] bytes = (contents != null) ? contents.getBytes(StandardCharsets.UTF_8) : null;
			MemorySchemeURLStreamHandlerFactory.getInstance().putContents(url.toURI(), bytes);

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
