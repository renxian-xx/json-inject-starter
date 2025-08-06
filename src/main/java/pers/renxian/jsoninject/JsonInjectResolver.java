package pers.renxian.jsoninject;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import pers.renxian.jsoninject.annotation.JsonField;
import pers.renxian.jsoninject.annotation.JsonInject;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * JsonInject参数解析器
 * 该解析器用于处理带有@JsonInject注解的方法参数，将请求体中的JSON数据注入到方法参数中。
 * 支持两种模式：WHOLE（整个JSON对象）和PART（部分JSON对象）。
 */
@Component
public class JsonInjectResolver extends AbstractMessageConverterMethodArgumentResolver {

    /**
     * JsonInject的请求属性名称
     */
    private static final String JsonInject = "JsonInject";
    /**
     * JsonInject的作用域
     * 0表示请求作用域，1表示会话作用域，2表示全局作用域
     */
    private static final int Scope = 0;
    /**
     * 包装JSON对象的键
     */
    private static final String Key = "json";

    public JsonInjectResolver(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查方法参数是否有@JsonInject注解
        return parameter.getMethod().isAnnotationPresent(JsonInject.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        parameter = parameter.nestedIfOptional();
        Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
        // 参照@RequestBody的处理方式，进行数据绑定和验证
        String name = Conventions.getVariableNameForParameter(parameter);

        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
            if (arg != null) {
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }
            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
            }
        }

        return adaptArgumentIfNecessary(arg, parameter);
    }

    @Override
    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter, Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        // 获取ServletWebRequest对象
        ServletWebRequest request = (ServletWebRequest) webRequest;
        // 获取方法参数的类型
        Type type = parameter.getGenericParameterType();

        // 从请求属性中获取已解析的JSON对象，如果不存在则解析请求体中的JSON
        JSONObject json;
        if ((json = (JSONObject) request.getAttribute(JsonInject, Scope)) == null) {
            ServletInputStream stream = request.getRequest().getInputStream();
            json = JSONObject.parseObject(getJsonString(stream));
            request.setAttribute(JsonInject, json, Scope);
        }

        // 获取方法参数的注解和模式
        Method method = parameter.getMethod();
        JsonInject annotation = method.getAnnotation(JsonInject.class);
        // 获取注解的值，决定注入模式
        JsonInjectMode mode = annotation.value();
        // 返回对象默认为null
        Object arg = null;
        // 如果模式是WHOLE，则直接返回整个JSON对象
        if (mode == JsonInjectMode.WHOLE) {
            arg = json.getObject(Key, type);
        } else if (mode == JsonInjectMode.PART) {
            // 如果模式是PART，则根据注解或参数名获取特定字段的值
            String name;
            JsonField field;
            json = json.getJSONObject(Key);
            // 检查参数是否有@JsonField注解，如果有则使用注解中的值作为字段名，否则使用参数名
            if ((field = parameter.getParameterAnnotation(JsonField.class)) != null)
                name = field.value();
            else
                name = parameter.getParameterName();
            // 如果JSON对象中存在该字段，进行解析并返回
            if (json != null && json.containsKey(name)) {
                // 返回指定字段的值，转换为方法参数的类型
                arg = json.getObject(name, type);
            }
        }
        return arg;
    }

    /**
     * 从ServletInputStream中读取JSON字符串
     *
     * @param stream ServletInputStream对象
     * @return 读取的JSON字符串
     * @throws IOException IO异常
     */
    private static String getJsonString(ServletInputStream stream) throws IOException {
        byte[] bytes = new byte[2048];
        int len;
        StringBuilder s = new StringBuilder();
        while ((len = stream.read(bytes)) != -1) {
            s.append(new String(bytes, 0, len));
        }
        stream.close();
        if (s.length() == 0)
            return "{}";
        return "{" + Key + ":" + s + "}";

    }


}
