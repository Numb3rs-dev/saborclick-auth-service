package com.saborclick.auth.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class SecureIdService {

    private static final SecureRandom random = new SecureRandom();
    private static final int DEFAULT_LENGTH = 10;
    private static final String SEPARATOR = ":";
    private static final String HMAC_ALGO = "HmacSHA256";

    private final String secret;

    public SecureIdService(@Value("${security.secret-key}") String secret) {
        this.secret = secret;
    }

    public String generateSecureId(String id, String sessionHash) {
        String base = id + SEPARATOR + sessionHash;
        String signature = hmac(base);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((base + SEPARATOR + signature).getBytes());
    }

    public String verifyAndExtract(String secureId, String sessionHash) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(secureId);
            String[] parts = new String(decoded).split(SEPARATOR);

            if (parts.length != 3) {
                throw new IllegalArgumentException("Formato de ID firmado inválido");
            }

            String id = parts[0];
            String hash = parts[1];
            String providedSignature = parts[2];

            if (!hash.equals(sessionHash)) {
                throw new IllegalArgumentException("Hash de sesión inválido");
            }

            String expected = hmac(id + SEPARATOR + hash);
            if (!expected.equals(providedSignature)) {
                throw new IllegalArgumentException("Firma inválida");
            }

            return id;

        } catch (Exception e) {
            log.error("Error verificando SecureId: {}", e.getMessage());
            throw new IllegalArgumentException("ID firmado inválido", e);
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), HMAC_ALGO);
            mac.init(keySpec);
            byte[] raw = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Error generando firma HMAC", e);
        }
    }
}
