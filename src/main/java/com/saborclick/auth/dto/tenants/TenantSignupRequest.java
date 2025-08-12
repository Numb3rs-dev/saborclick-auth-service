package com.saborclick.auth.dto.tenants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSignupRequest {

    // Datos del Tenant
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String tenantName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private String name;
    private String lastName;
    private String mobilePhone;
    private String country;
    private String city;
}
