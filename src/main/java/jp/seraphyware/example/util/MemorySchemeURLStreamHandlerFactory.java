package jp.seraphyware.example.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * メモリ上に保存されている「memory://server/path」スキーマを扱えるようにするためのURLストリームハンドラファクトリ.<br>
 * アプリケーション開始時に以下のようにURLに対してストリームハンドラファクトリを設定する。<br>
 * <pre>{@code
 * URL.setURLStreamHandlerFactory(MemorySchemeURLStreamHandlerFactory.getInstance());
 * }</pre>
 */
public class MemorySchemeURLStreamHandlerFactory implements URLStreamHandlerFactory {
	
	/**
	 * コンテンツとMIMEのペア
	 */
	public interface ContentsBody {
		
		/**
		 * 既定のMIME
		 */
		String DEFAULT_MIME = "application/octet-stream";

		/**
		 * コンテンツ
		 */
		byte[] getBytes();
		
		/**
		 * MIME
		 */
		String getContentType();
	}
	
	/**
	 * コンテンツとMIMEのペア
	 */
	public static final class SimpleContentsBody implements ContentsBody {
		
		/**
		 * MIME
		 */
		private final String mime;
		
		/**
		 * コンテンツ
		 */
		private final byte[] bytes;
		
		public SimpleContentsBody(String mime, byte[] contents) {
			this.mime = Objects.requireNonNull(mime);
			this.bytes = Objects.requireNonNull(contents);
		}
		
		@Override
		public byte[] getBytes() {
			return bytes;
		}
		
		@Override
		public String getContentType() {
			return mime;
		}
	}
	
	/**
	 * インメモリ・データベースとしてつかうハッシュ
	 */
	private final ConcurrentHashMap<URI, ContentsBody> inmemoryMap = new ConcurrentHashMap<>();

	/**
	 * シングルトン
	 */
	private static final MemorySchemeURLStreamHandlerFactory INST = new MemorySchemeURLStreamHandlerFactory();
	
	/**
	 * インスタンスを取得する
	 * @return
	 */
	public static MemorySchemeURLStreamHandlerFactory getInstance() {
		return INST;
	}

	/**
	 * プライベートコンストラクタ
	 */
	private MemorySchemeURLStreamHandlerFactory() {
		super();
	}

	/**
	 * 指定したURLにコンテンツをメモリ上に設定する。
	 * @param url 
	 * @param contents コンテンツ、nullの場合は削除扱いとなる
	 */
	public void putContents(URI url, byte[] contents) {
		putContents(url, ContentsBody.DEFAULT_MIME, contents);
	}

	/**
	 * 指定したURLにコンテンツをメモリ上に設定する
	 * @param url
	 * @param mime コンテンツタイプを指定する
	 * @param contents コンテンツ、nullの場合は削除扱いとなる
	 */
	public void putContents(URI url, String mime, byte[] contents) {
		if (url == null) {
			throw new IllegalArgumentException("url must be non-null");
		}
		if (contents == null) {
			inmemoryMap.remove(url);
			return;
		}
		putContents(url, new SimpleContentsBody(mime, contents));
	}
	
	/**
	 * 指定したURLにコンテンツをメモリ上に設定する
	 * @param url
	 * @param contents コンテンツ、nullの場合は削除扱いとなる
	 */
	public void putContents(URI url, ContentsBody contentsBody) {
		inmemoryMap.put(url, contentsBody);
	}
	
	/**
	 * 指定したURLをメモリ上から削除する
	 * @param url
	 */
	public void removeContents(URI url) {
		inmemoryMap.remove(url);
	}

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if ("memory".equals(protocol))
			return new URLStreamHandler() {

				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					ContentsBody contentsBody;
					try {
						contentsBody = inmemoryMap.get(u.toURI());
					} catch (URISyntaxException e) {
						throw new IOException("invalid url: " + u, e);
					}
					return new URLConnection(u) {

						@Override
						public void connect() throws IOException {
							if (contentsBody == null) {
								throw new IOException("missing contents: " + u);
							}
						}

						@Override
						public InputStream getInputStream() throws IOException {
							if (contentsBody == null) {
								throw new IOException("missing contents: " + u);
							}
							return new ByteArrayInputStream(contentsBody.getBytes());
						}

						@Override
						public String getContentType() {
							if (contentsBody != null) {
								return contentsBody.getContentType();
							}
							return ContentsBody.DEFAULT_MIME;
						}
					};
				}
			};
		return null;
	}
}