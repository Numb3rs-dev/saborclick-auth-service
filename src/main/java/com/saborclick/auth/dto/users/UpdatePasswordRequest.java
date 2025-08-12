package com.saborclick.auth.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @NotBlank(message = "El ID del usuario es obligatorio")
    private String userId;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;
}


