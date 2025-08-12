package com.saborclick.auth.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiKeyValidator {

    @Value("${security.password-change-plan}")
    private String planPassword;

    public boolean isValidPlanChangePassword(String planPasswordRequest) {
        return planPasswordRequest != null && planPasswordRequest.equals(planPassword);
    }
}
