package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.config.ApiKeySecurityConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@ApplicationScoped
public class ApiKeyHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Inject
    private ApiKeySecurityConfig config;

    public String hmacSha256(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (config.getHmacSecret() == null || config.getHmacSecret().isBlank()) {
            throw new IllegalStateException("apikey.secret is required");
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(config.getHmacSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return toHex(mac.doFinal(rawApiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash API key", exception);
        }
    }

    public boolean verify(String rawApiKey, String storedHash) {
        if (rawApiKey == null || rawApiKey.isBlank() || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        byte[] expected = fromHex(storedHash);
        if (expected.length == 0) {
            return false;
        }
        byte[] actual = fromHex(hmacSha256(rawApiKey));
        return MessageDigest.isEqual(actual, expected);
    }

    private String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
    }

    private byte[] fromHex(String hex) {
        if (hex.length() % 2 != 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int index = 0; index < hex.length(); index += 2) {
            try {
                bytes[index / 2] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
            } catch (NumberFormatException exception) {
                return new byte[0];
            }
        }
        return bytes;
    }
}
