package jp.seraphyware.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(ManifestHelper.class);

	private ManifestHelper() {
		super();
	}
	
	/**
	 * 指定したクラスを保持しているMETA-INF/MANIFEST.MF情報を取得する。
	 * (jarまたはfileのいずれの場所にあっても取得可能である。)
	 * @param cls クラス
	 * @return 魔にフェススト
	 */
	private static Manifest loadManifest(Class<?> cls) {
		// このクラスを格納しているjarの中のMANIFESTファイルを読み取る
		URL res = cls.getResource(cls.getSimpleName() + ".class");
		String s = res.toString();
		try {
			res = new URL(s.substring(0, s.length() - (cls.getName() + ".class").length()) + "META-INF/MANIFEST.MF");
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		logger.info("MANIFEST-URL=" + res);

		Manifest mf = new Manifest();
		try (InputStream is = res.openStream()) { // 開くまで実在するか分からないため事前チェックはできない
			mf.read(is);

		} catch (IOException ex) {
			logger.warn("failed to read {}", res, ex);
		}

		Attributes attrs = mf.getMainAttributes();
		for (Map.Entry<Object, Object> entry : attrs.entrySet()) {
			logger.info(">" + entry);
		}

		return mf;
	}

	/**
	 * このモジュールの実装バージョンを取得する。存在しない場合はdevelopを返す。
	 * (java9以降は、他のモジュールのプライベートにはアクセスできないため、自分自身のみを想定している。)
	 * @return
	 */
	public static String getImplementationVersion() {
		try {
			Manifest mf = loadManifest(ManifestHelper.class);

			// マニフェストの実装バージョンの取得
			Attributes attrs = mf.getMainAttributes();
			String implVersion = attrs.getValue("Implementation-Version");
			return implVersion == null ? "develop" : implVersion;

		} catch (Exception ex) {
			logger.error("failed to get implementation-version", ex);
			return ex.toString();
		}
	}
}
