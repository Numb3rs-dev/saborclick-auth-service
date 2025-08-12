package com.saborclick.auth.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotBlank(message = "El ID del usuario es obligatorio")
    private String userId;

    private boolean enable;
}
