package com.marco.rentflow.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica la regla a todos los endpoints de tu API
                .allowedOrigins("http://localhost:5173") // Origen permitido: futuro frontend en Vite/React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP habilitados
                .allowedHeaders("*") // Permite cualquier cabecera (incluyendo Authorization para JWT más adelante)
                .allowCredentials(true); // Necesario si envías cookies o tokens de sesión
    }
}
