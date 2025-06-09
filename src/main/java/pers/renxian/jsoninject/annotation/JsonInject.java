package pers.renxian.jsoninject.annotation;


import pers.renxian.jsoninject.JsonInjectMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记方法，指示该方法的参数应从JSON请求体中注入数据。
 * 支持两种注入模式：WHOLE（整个JSON对象）和PART（部分JSON对象）。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonInject {
    JsonInjectMode value() default JsonInjectMode.PART;

}
