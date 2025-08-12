package com.saborclick.auth.dto.permissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class PermissionsResponse {
    private Map<String, Map<String, String>> modules; // módulo -> (clave -> valor)
}