package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@ApplicationScoped
public class ApiKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] PUBLIC_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int PUBLIC_ID_LENGTH = 8;
    private static final int SECRET_BYTES = 32;
    private static final int CLAIM_CODE_GROUPS = 5;
    private static final int CLAIM_CODE_GROUP_LENGTH = 4;

    public String generateApiKey(Environment environment) {
        return generateApiKeyMaterial(environment).apiKey();
    }

    public GeneratedApiKey generateApiKeyMaterial(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment is required");
        }

        String publicId = randomPublicId();
        String keyPrefix = "ak_"
                + environment.name().toLowerCase(Locale.ROOT)
                + "_"
                + publicId;
        String secret = randomSecret();
        return new GeneratedApiKey(keyPrefix, keyPrefix + "." + secret);
    }

    public String generateClaimCode() {
        StringBuilder claimCode = new StringBuilder();
        for (int group = 0; group < CLAIM_CODE_GROUPS; group++) {
            if (group > 0) {
                claimCode.append("-");
            }
            for (int index = 0; index < CLAIM_CODE_GROUP_LENGTH; index++) {
                claimCode.append(PUBLIC_ID_ALPHABET[RANDOM.nextInt(PUBLIC_ID_ALPHABET.length)]);
            }
        }
        return claimCode.toString();
    }

    private String randomPublicId() {
        StringBuilder publicId = new StringBuilder(PUBLIC_ID_LENGTH);
        for (int index = 0; index < PUBLIC_ID_LENGTH; index++) {
            publicId.append(PUBLIC_ID_ALPHABET[RANDOM.nextInt(PUBLIC_ID_ALPHABET.length)]);
        }
        return publicId.toString();
    }

    private String randomSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record GeneratedApiKey(String keyPrefix, String apiKey) {
    }
}
