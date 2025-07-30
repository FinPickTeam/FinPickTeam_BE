package org.scoula.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

@EnableWebMvc
@ComponentScan(basePackages = {
        "org.scoula.exception",
        "org.scoula.controller",
        "org.scoula.finance.controller",
        "org.scoula.user.controller",
        "org.scoula.user.exception",
        "org.scoula.transactions.controller",
        "org.scoula.survey.controller",
        "org.scoula.quiz.controller",
        "org.scoula.dictionary.controller",
        "org.scoula.bubble.controller",
        "org.scoula.news.controller",
        "org.scoula.transactions.exception",
        "org.scoula.nhapi.controller",
        "org.scoula.challenge.controller",
        "org.scoula.challenge.exception"
})
public class ServletConfig implements WebMvcConfigurer {

    // Jackson 컨버터 등록
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/")
                .setViewName("forward:/resources/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/resources/assets/");

        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
