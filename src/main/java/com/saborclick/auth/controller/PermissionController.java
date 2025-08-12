package com.saborclick.auth.controller;

import com.saborclick.auth.dto.permissions.PermissionsResponse;
import com.saborclick.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<PermissionsResponse> getPermissionsByTenant(@PathVariable String tenantId) {
        PermissionsResponse response = permissionService.getPermissionsByTenantId(tenantId);
        return ResponseEntity.ok(response);
    }
}