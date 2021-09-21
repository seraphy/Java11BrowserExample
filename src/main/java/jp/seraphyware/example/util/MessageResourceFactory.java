package jp.seraphyware.example.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

/**
 * メッセージリソースをロードするためのファクトリクラス。
 */
@ApplicationScoped
public class MessageResourceFactory {

	private static final Logger logger = LoggerFactory.getLogger(MessageResourceFactory.class);
	
	private Map<String, ResourceBundle> cachedBundles = new HashMap<>();
	
	@Produces
	@MessageResource
	@Dependent
	public ResourceBundle getMessages(InjectionPoint ip) {
		MessageResource prefixAnnt = ip.getAnnotated().getAnnotation(MessageResource.class);
		String resName = prefixAnnt.value();

		// リソースバンドルは一度読み込まれたら変更されない性質なのでキャッシュしておく
		return cachedBundles.computeIfAbsent(resName, this::loadResourceBundle);
	}
	
	/**
	 * リソースバンドルをロードする
	 * @param resName
	 * @return
	 */
	private ResourceBundle loadResourceBundle(String resName) {
		logger.info("load resourcebundle: {}", resName);
		
		if (resName.endsWith(".xml")) {
			// xml形式でのロード
			// java9以降の名前付きモジュールでは、Control派生クラスのリソースバンドルでの利用がサポートされていないため、
			// 明示的にxmlのコントロールを利用することで代替とする。
			XMLResourceBundleControl xmlReader = new XMLResourceBundleControl();
			ClassLoader clsldr = getClass().getClassLoader();
			try {
				String simpleResName = resName.substring(0, resName.length() - 4);
				Locale defLocale = Locale.getDefault();
				// 検索するロケールサフィックスを準備する
				List<Locale> candidateLocales = new ArrayList<>(xmlReader.getCandidateLocales(simpleResName, defLocale));
				candidateLocales.add(Locale.ROOT); // サフィックスなしを最後に検索する
				for (Locale locale : candidateLocales) {
					ResourceBundle bundle = xmlReader.newBundle(simpleResName, locale, "xml", clsldr, true);
					logger.info("find-resource: locale={}, result={}", locale, bundle);
					if (bundle != null) {
						return bundle;
					}
				}
		        throw new MissingResourceException("Can't find bundle for base name "
                        + simpleResName + ", locale " + defLocale,
                        simpleResName + "_" + defLocale, // className
                        "");

			} catch (IllegalAccessException | InstantiationException | IOException ex) {
				logger.error("failed to read resource-bundle {}", resName, ex);
				throw new RuntimeException(ex);
			}
		}

		// プロパティファイル形式(*.properties)のロード
		return ResourceBundle.getBundle(resName);
	}
}
