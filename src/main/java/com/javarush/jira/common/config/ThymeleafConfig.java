package com.javarush.jira.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
//http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html
@RequiredArgsConstructor
public class ThymeleafConfig {
    private final AppProperties appProperties;

    /**
     * Bean with RU locale.
     *
     * @return Create a new RU locale.
     */
    @Bean
    public Locale getDefaultLocale() {
        return new Locale("ru", "RU");
    }

    /**
     * @return Bean LocaleResolver with default locale.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(getDefaultLocale());
        return slr;
    }

    /**
     * Interceptor bean that will switch to a new
     * locale based on the value of the "lang"
     * parameter when present on the request.
     *
     * @return LocaleChangeInterceptor.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }
}
