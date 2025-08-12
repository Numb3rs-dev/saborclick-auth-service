package com.saborclick.auth.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteUserRequest {

    @NotBlank(message = "El ID del usuario es obligatorio")
    private String userId;

    private String reason; // Opcional, para registrar el motivo de eliminación
}
