package com.backendSeaBattle.sea_battle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS-конфигурация: жёстко разрешаем доступ для фронтенда по HTTPS,
 * а также для локальной отладки (localhost:3000).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration reg = registry.addMapping("/api/**")
            // Жёстко разрешаем только эти два origin:
            .allowedOrigins(
                "https://bestseabattle.netlify.app", // продакшен-фронтенд
                "http://localhost:3000"               // для локального тестирования
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowCredentials(true)
            .allowedHeaders("*");
        // Если нужно ещё что-то (например, maxAge), можно добавить:
        // reg.maxAge(3600);
    }
}
