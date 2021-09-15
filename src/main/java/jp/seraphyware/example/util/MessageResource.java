package jp.seraphyware.example.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

/**
 * CDIによりResourceBundleをInjectするとき、message-resoufce.xmlのリソースファイルを引き当てるためのQualifierマーカー
 */
@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface MessageResource {

	/**
	 * リソース名、デフォルトは「message-resource.xml」
	 * @return
	 */
	@Nonbinding
	String value() default "message-resource.xml";
}
