package pers.renxian.jsoninject.annotation;

import java.lang.annotation.*;

/**
 * 用于标记方法参数，指示该参数应从JSON请求体中注入特定字段的值。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JsonField {

    String value();
}
