package pers.renxian.jsoninject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * JsonInject自动配置类
 */
@ConditionalOnWebApplication
@Import({JsonInjectResolver.class})
public class JsonInjectAutoConfiguration implements WebMvcConfigurer {

    private final JsonInjectResolver jsonInjectResolver;

    @Autowired
    public JsonInjectAutoConfiguration(JsonInjectResolver jsonInjectResolver) {
        this.jsonInjectResolver = jsonInjectResolver;
    }

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.jsonInjectResolver);
    }
}