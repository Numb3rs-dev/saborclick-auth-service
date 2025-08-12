package com.saborclick.auth.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String userName;

    @Email(message = "Formato de correo inválido")
    private String email;

    private String name;

    private String lastName;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private String mobilePhone;
    private String country;
    private String city;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "TENANT_USER|TENANT_ADMIN", message = "Rol inválido: solo se permite TENANT_USER o TENANT_ADMIN")
    private String rol; // validado manualmente con Rol.valueOf(...) en el service
}
