package com.backendSeaBattle.sea_battle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS-конфигурация: разрешённые origin из переменной окружения.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Возьмёт значение из application-prod.properties: myapp.cors.allowed-origins
    @Value("${myapp.cors.allowed-origins:}")
    private String allowedOrigins; // 

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Если allowedOrigins пусто, то CORS не настраивается, иначе разбиваем по запятой
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return;
        }
        String[] originsArray = allowedOrigins.split(",");

        CorsRegistration reg = registry.addMapping("/api/**")
                .allowedOrigins(originsArray)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowCredentials(true)
                .allowedHeaders("*");
        // Здесь можно добавить любые другие настройки CORS, если нужно
    }
}
