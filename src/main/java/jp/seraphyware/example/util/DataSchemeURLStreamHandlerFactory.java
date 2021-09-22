package jp.seraphyware.example.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dataプロトコルをURL「data:text/plain,hello%20world」「data:image/jpeg;BASE64...」形式としてハンドリングできるようにするためのURLストリームハンドラファクトリ.<br>
 * アプリケーション開始時に以下のようにURLに対してストリームハンドラファクトリを設定する。<br>
 * <pre>{@code
 * URL.setURLStreamHandlerFactory(new DataSchemeURLStreamHandlerFactory());
 * }</pre>
 */
public class DataSchemeURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private static final Logger logger = LoggerFactory.getLogger(DataSchemeURLStreamHandlerFactory.class);
	
	private static final String DEFAULT_MIME =  "application/octet-stream";
	
	/**
	 * カスケードする場合のファクトリ、null可
	 */
	private URLStreamHandlerFactory parent;
	
	/**
	 * デフォルトのコンストラクタ
	 */
	public DataSchemeURLStreamHandlerFactory() {
		this(null);
	}

	/**
	 * ファクトリをカスケードする場合のコンストラクタ
	 * @param parent 親ファクトリ、null可
	 */
	public DataSchemeURLStreamHandlerFactory(URLStreamHandlerFactory parent) {
		this.parent = parent;
	}
	
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if ("data".equals(protocol)) {
			return new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					String path = u.getPath();
					
					int pt_camma = path.indexOf(','); // コンテンツ前の間切り

					String contentType;
					String encoding;
					if (pt_camma >= 0) {
						String spec = path.substring(0, pt_camma).trim();
						path = path.substring(pt_camma + 1);
						
						int pt_semi = spec.indexOf(';'); // エンコード方式
						if (pt_semi >= 0) {
							encoding = spec.substring(pt_semi + 1);
							spec = spec.substring(0, pt_semi);
						} else {
							encoding = null;
						}

						if (spec == null || spec.length() == 0) {
							contentType = DEFAULT_MIME;
						} else {
							contentType = spec;
						}
					} else {
						contentType = DEFAULT_MIME;
						encoding = null;
					}
					
					byte[] data;
					if (encoding == null || encoding.trim().length() == 0) {
						// URLエンコード方式(デフォルト)
						String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
						data = decoded.getBytes(StandardCharsets.UTF_8);
						logger.info("data url: contentType={}, data={}", contentType, decoded);

					} else if ("base64".equalsIgnoreCase(encoding)) {
						// base64形式
						data = Base64.getDecoder().decode(path);
						logger.info("data url: contentType={}, data.length={}", contentType, data.length);

					} else {
						// 不明な形式
						throw new IOException("unsupported encoding: " + u);
					}

					return new URLConnection(u) {
						@Override
						public void connect() throws IOException {
						}

						@Override
						public InputStream getInputStream() throws IOException {
							return new ByteArrayInputStream(data);
						}

						@Override
						public String getContentType() {
							return contentType;
						}
					};
				}
			};
		}
		
		return (parent != null) ? parent.createURLStreamHandler(protocol) : null;
	}
}
