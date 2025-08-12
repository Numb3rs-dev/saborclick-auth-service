package com.saborclick.auth.service;

import com.saborclick.auth.common.exception.ForbiddenException;
import com.saborclick.auth.common.exception.UnauthorizedException;
import com.saborclick.auth.common.security.SecureIdService;
import com.saborclick.auth.common.security.jwt.TokenBlacklistService;
import com.saborclick.auth.dto.*;
import com.saborclick.auth.dto.auth.LoginRequest;
import com.saborclick.auth.entity.*;
import com.saborclick.auth.enums.Rol;
import com.saborclick.auth.mail.MailService;
import com.saborclick.auth.repository.*;
import com.saborclick.auth.common.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final SecureIdService secureIdService;
    private final TokenBlacklistService tokenBlacklistService;

    public TokenResponse login(LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getUserName());

        Optional<User> optionalUser = userRepository.findByUserName(request.getUserName());

        if (optionalUser.isEmpty()) {
            log.warn("Usuario no registrado: {}", request.getUserName());
            throw new BadCredentialsException("Credenciales inválidas");
        }

        User user = optionalUser.get();

        if (user.isAccountLocked()) {
            if (user.getLockUntil() != null && user.getLockUntil().before(new Date())) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
                log.info("Usuario {} desbloqueado automáticamente", user.getUsername());
            } else {
                log.warn("Usuario {} está bloqueado hasta {}", user.getUsername(), user.getLockUntil());
                throw new UnauthorizedException("Cuenta bloqueada. Intenta más tarde.");
            }
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
            );

            user = (User) auth.getPrincipal();

            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockUntil(null);
            userRepository.save(user);

            String token = jwtService.generateToken(user);
            log.info("Login exitoso para {}", user.getEmail());

            return TokenResponse.builder().token(token).build();

        } catch (DisabledException e) {
            log.warn("🚫 Usuario deshabilitado: {}", request.getUserName());
            throw new ForbiddenException("Tu cuenta aún no ha sido activada. Revisa tu correo.");

        }catch (BadCredentialsException e) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setLockUntil(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
                log.warn("Usuario {} bloqueado por exceder intentos", user.getEmail());
            }
            userRepository.save(user);
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }


    public void revokeToken(String token) {
        Claims claims = jwtService.parseAllClaims(token);
        Date expiration = claims.getExpiration();
        long millisLeft = expiration.getTime() - System.currentTimeMillis();

        if (millisLeft > 0) {
            tokenBlacklistService.revokeToken(token, millisLeft);
        } else {
            log.info("Token ya estaba expirado, no se guardó en blacklist.");
        }
    }

}
