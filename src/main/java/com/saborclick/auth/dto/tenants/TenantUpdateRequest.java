package com.saborclick.auth.dto.tenants;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantUpdateRequest {

    @NotBlank(message = "El nombre del restaurante no puede estar vacío")
    private String name;

    // ❌ El email no puede cambiarse porque es la llave principal lógica
    // Por eso no lo incluimos en este DTO

    private String phone;
    private String address;
    private String addressLine2;
    private String city;
    private String country;
    private Double lat;
    private Double lon;
    private String zone;
}
