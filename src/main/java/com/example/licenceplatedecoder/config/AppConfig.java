package com.example.licenceplatedecoder.config;

import org.apache.catalina.security.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({GeminiConfig.class})
public class AppConfig {
}
