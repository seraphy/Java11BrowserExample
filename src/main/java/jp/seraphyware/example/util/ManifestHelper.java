package jp.seraphyware.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * マニフェストを取得するヘルパ
 */
public class ManifestHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(ManifestHelper.class);

	private static final Map<String, Manifest> manifestMap = new ConcurrentHashMap<>();
	
	private ManifestHelper() {
		super();
	}
	
	/**
	 * 指定したクラスを保持しているMETA-INF/MANIFEST.MF情報を取得する。
	 * (jarまたはfileのいずれの場所にあっても取得可能である。)
	 * @param cls クラス
	 * @return 魔にフェススト
	 */
	public static Manifest loadManifest(Class<?> cls) {
		// このクラスのクラスファイルがある位置を求める
		URL clsLocation = cls.getResource(cls.getSimpleName() + ".class");
		if (clsLocation == null) {
			throw new IllegalArgumentException("クラス名からクラスファイルがわかりません。" + cls.getSimpleName());
		}
		// このクラスを格納しているjarの中のMANIFESTファイルの位置を特定する
		String s = clsLocation.toString();
		String manifestLocationStr = s.substring(0, s.length() - (cls.getName() + ".class").length()) + "META-INF/MANIFEST.MF";
		logger.info("MANIFEST-URL=" + manifestLocationStr);

		return manifestMap.computeIfAbsent(manifestLocationStr, uri -> {
			Manifest mf = new Manifest();
			try {
				URL res = new URL(manifestLocationStr);
				try (InputStream is = res.openStream()) { // 開くまで実在するか分からないため事前チェックはできない
					mf.read(is);
				}
			} catch (IOException ex) {
				logger.warn("failed to read {}", manifestLocationStr, ex);
			}
			return mf;
		});
	}
	
	/**
	 * マニフェストの実装バージョンを取得する。存在しない場合はdevelopを返す。
	 * @return
	 */
	public static String getImplementationVersion(Manifest mf) {
		// マニフェストの実装バージョンの取得
		Attributes attrs = mf.getMainAttributes();
		String implVersion = attrs.getValue("Implementation-Version");
		return implVersion == null ? "develop" : implVersion;
	}

	/**
	 * このモジュールの実装バージョンを取得する。存在しない場合はdevelopを返す。
	 * @return
	 */
	public static String getImplementationVersion() {
		try {
			Manifest mf = loadManifest(ManifestHelper.class);
			return getImplementationVersion(mf);

		} catch (Exception ex) {
			logger.error("failed to get implementation-version", ex);
			return ex.toString();
		}
	}

	/**
	 * マニフェストの主要項目を文字列としてプリントする。
	 * @param mf マニフェスト
	 * @return 文字列
	 */
	public static String toString(Manifest mf) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Attributes attrs = mf.getMainAttributes();
		for (Map.Entry<Object, Object> entry : attrs.entrySet()) {
			pw.println(entry.getKey() + "=" + entry.getValue());
		}
		pw.flush();
		return sw.toString();
	}
}
