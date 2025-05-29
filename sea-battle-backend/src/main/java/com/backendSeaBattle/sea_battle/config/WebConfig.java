/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Александра
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${ngrok.enabled:false}")
    private boolean ngrokEnabled;

    @Value("${ngrok.public-url:}")
    private String publicUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration reg = registry.addMapping("/api/**")
                // Разрешаем запросы с localhost:5173 (ваш фронтенд)...
                .allowedOrigins(
                        "https://3015-193-37-196-53.ngrok-free.app", // ← твой фронт
                        "http://localhost:3000" // ← если локально тестируешь
                );// Если вы используете ngrok (фронтенд тоже может обращаться по ngrok), добавляем и его:
        if (ngrokEnabled && !publicUrl.isBlank()) {
            reg.allowedOrigins(publicUrl);
        }

        reg.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowCredentials(true)
                .allowedHeaders("*");
    }
}
