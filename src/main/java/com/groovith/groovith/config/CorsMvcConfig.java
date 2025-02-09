package com.groovith.groovith.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowCredentials(true)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowedOriginPatterns(
                        "http://localhost",
                        "http://localhost:5173",
                        "http://3.38.237.6",
                        "http://groovith.com",
                        "https://groovith.com"
                )
                .exposedHeaders("Authorization", "");
    }
}
