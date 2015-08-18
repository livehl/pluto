package cn.city.in.task.manager.http.comment;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * http参数注解
 * 
 * @author 黄林 The Interface HttpParam.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpParam {

	/**
	 * 默认值
	 * 
	 * @return the string
	 * @author 黄林
	 */
	String defaultValue() default "";

	/**
	 * 该参数是否必须
	 * 
	 * @return true, if successful
	 * @author 黄林
	 */
	boolean required() default true;

	/**
	 * 需要绑定的参数.
	 * 
	 * @return the string
	 * @author 黄林
	 */
	String value();
}
