package com.saborclick.auth.service;

import com.saborclick.auth.common.exception.UnauthorizedException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaptchaValidator {

    @Value("${security.captcha.secret-key}")
    private String captchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void validateCaptcha(String token) {
        String url = "https://www.google.com/recaptcha/api/siteverify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", captchaSecret);
        params.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        CaptchaResponse response = restTemplate.postForObject(url, request, CaptchaResponse.class);
        if (response == null || !response.success) {
            throw new UnauthorizedException("Captcha inválido");
        }
    }

    @Data
    public static class CaptchaResponse {
        private boolean success;
        private List<String> errorCodes;
    }
}

