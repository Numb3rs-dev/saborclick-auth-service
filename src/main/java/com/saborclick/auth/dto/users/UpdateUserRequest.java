package com.saborclick.auth.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "El ID del usuario es obligatorio")
    private String userId; // ✅ ID seguro

    private String name;
    private String lastName;
    private String phone;
    private String mobilePhone;
    private String country;
    private String city;
    private String address;
    private String addressLine2;
    private Double lat;
    private Double lon;
    private String zone;

    @Pattern(regexp = "TENANT_USER|TENANT_ADMIN", message = "Rol inválido: solo se permite TENANT_USER o TENANT_ADMIN")
    private String rol;
}