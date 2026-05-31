package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApiKeySecurityConfig {

    @ConfigProperty(name = "apikey.secret")
    String hmacSecret;

    public String getHmacSecret() {
        return hmacSecret;
    }
}
