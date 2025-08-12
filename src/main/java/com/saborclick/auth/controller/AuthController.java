package com.saborclick.auth.controller;

import com.saborclick.auth.common.exception.ForbiddenException;
import com.saborclick.auth.common.security.JwtService;
import com.saborclick.auth.dto.*;
import com.saborclick.auth.dto.auth.LoginRequest;
import com.saborclick.auth.dto.auth.NewPasswordRequest;
import com.saborclick.auth.dto.tenants.TenantSignupRequest;
import com.saborclick.auth.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final TenantService tenantService;
    private final UserService userService;

    @PostMapping("/public/signup")
    public ResponseEntity<SuccessResponse> signupTenant(
            @Valid @RequestBody TenantSignupRequest request,
            @RequestParam("captchaToken") String captchaToken
    ) {
        tenantService.registerTenantWithAdmin(request, captchaToken);
        return ResponseEntity.ok(
                SuccessResponse.builder()
                        .success(true)
                        .message("✅ Tu usuario ha sido creado exitosamente. Revisa tu correo para activar la cuenta.")
                        .build()
        );
    }

    @PostMapping("/public/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/public/logout")
    public ResponseEntity<SuccessResponse> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.revokeToken(token);
        return ResponseEntity.ok(
                SuccessResponse.builder()
                        .success(true)
                        .message("Sesión cerrada exitosamente")
                        .build()
        );
    }

    @PostMapping("/public/request-reset")
    public ResponseEntity<String> requestReset(@Valid @RequestBody ResetRequest request) {
        passwordService.sendTokenToRecovery(request.getEmail());
        return ResponseEntity.ok("Si el correo está registrado, se envió un enlace para restablecer la contraseña.");
    }

    @PostMapping("/public/update-password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody NewPasswordRequest request) {
        passwordService.updatePasswordWithToken(request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PostMapping("/public/activate")
    public ResponseEntity<?> activateUser(@RequestParam("token") String token) throws ForbiddenException {
        try {
            String userId = jwtService.validateActivationToken(token); // ya es String
            userService.activateUser(userId); // ✅ todo queda en String
            return ResponseEntity.ok("Usuario activado correctamente");
        } catch (ForbiddenException ex) {
            return ResponseEntity.status(403).body("Cuenta no autorizada o token inválido.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }

    @GetMapping("/public/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ AuthController cargado correctamente");
    }
}
