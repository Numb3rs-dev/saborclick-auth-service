package com.saborclick.auth.controller;

import com.saborclick.auth.common.exception.ForbiddenException;
import com.saborclick.auth.common.security.annotations.CurrentUserId;
import com.saborclick.auth.dto.ChangePlanRequest;
import com.saborclick.auth.dto.SuccessResponse;
import com.saborclick.auth.dto.tenants.TenantUpdateRequest;
import com.saborclick.auth.common.security.ApiKeyValidator;
import com.saborclick.auth.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final ApiKeyValidator apiKeyValidator;

    @PutMapping("/change-plan")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SuccessResponse> changePlan(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody ChangePlanRequest request
    ) {
        if (!apiKeyValidator.isValidPlanChangePassword(apiKey)) {
            throw new ForbiddenException("Acceso denegado: clave inválida");
        }

        return ResponseEntity.ok(SuccessResponse.builder()
                .success(true)
                .message("Perfil del tenant actualizado correctamente")
                .build());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<Void> updateCurrentTenantProfile(
            @RequestBody @Valid TenantUpdateRequest request,
            @CurrentUserId String currentUserId
    ) {
        tenantService.updateCurrentTenantProfile(request, currentUserId);
        return ResponseEntity.noContent().build();
    }


}
