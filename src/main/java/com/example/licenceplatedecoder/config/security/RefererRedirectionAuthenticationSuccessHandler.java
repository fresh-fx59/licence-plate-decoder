package com.example.licenceplatedecoder.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class RefererRedirectionAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public RefererRedirectionAuthenticationSuccessHandler() {
        setUseReferer(true);
    }
}
