// -------------------------
// DTOs utilizados
// -------------------------

package com.saborclick.auth.dto.auth;

import lombok.*;

// Login
@Getter @Setter
public class LoginRequest {
    private String userName;
    private String password;
}