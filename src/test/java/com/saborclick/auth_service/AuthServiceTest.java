package com.saborclick.auth_service;

import com.saborclick.auth.common.security.JwtService;
import com.saborclick.auth.common.security.SecureIdService;
import com.saborclick.auth.common.security.jwt.TokenBlacklistService;
import com.saborclick.auth.dto.auth.LoginRequest;
import com.saborclick.auth.dto.TokenResponse;
import com.saborclick.auth.entity.Tenant;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.enums.Rol;
import com.saborclick.auth.mail.MailService;
import com.saborclick.auth.repository.*;
import com.saborclick.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private PlanRepository planRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private MailService mailService;
    @Mock private SecureIdService secureIdService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginExitoso() {
        LoginRequest request = new LoginRequest();
        request.setUserName("admin");
        request.setPassword("123456");

        User mockUser = User.builder()
                .userName("admin")
                .password("123456")
                .email("admin@email.com")
                .rol(Rol.SUPER_ADMIN)
                .isActive(true)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .tenant(Tenant.builder().id("tnt_123").name("Tenant 1").build())
                .build();

        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));
        when(jwtService.generateToken(mockUser)).thenReturn("token123");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        verify(userRepository).save(mockUser);
    }

    @Test
    void loginUsuarioNoEncontrado() {
        LoginRequest request = new LoginRequest();
        request.setUserName("notfound");
        request.setPassword("1234");

        when(userRepository.findByUserName("notfound")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginUsuarioBloqueado() {
        LoginRequest request = new LoginRequest();
        request.setUserName("bloqueado");
        request.setPassword("1234");

        User mockUser = User.builder()
                .userName("bloqueado")
                .accountLocked(true)
                .lockUntil(new Date(System.currentTimeMillis() + 10000))
                .build();

        when(userRepository.findByUserName("bloqueado")).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }
}
