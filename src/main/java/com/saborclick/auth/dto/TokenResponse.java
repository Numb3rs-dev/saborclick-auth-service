// -------------------------
// DTOs utilizados
// -------------------------

package com.saborclick.auth.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {
    private String token;
}