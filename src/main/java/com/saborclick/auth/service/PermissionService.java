package com.saborclick.auth.service;

import com.saborclick.auth.dto.permissions.PermissionsResponse;
import com.saborclick.auth.entity.PlanPermission;
import com.saborclick.auth.entity.Tenant;
import com.saborclick.auth.repository.PlanPermissionRepository;
import com.saborclick.auth.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PlanPermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;

    public PermissionsResponse getPermissionsByTenantId(String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant no encontrado"));

        String planId = tenant.getPlan().getId();
        List<PlanPermission> permissions = permissionRepository.findByPlanId(planId);

        Map<String, Map<String, String>> grouped = new HashMap<>();
        for (PlanPermission permission : permissions) {
            grouped
                    .computeIfAbsent(permission.getModule(), k -> new HashMap<>())
                    .put(permission.getKey(), permission.getValue());
        }

        return new PermissionsResponse(grouped);
    }
}