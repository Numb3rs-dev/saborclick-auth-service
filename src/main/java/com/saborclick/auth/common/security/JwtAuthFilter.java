package com.saborclick.auth.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saborclick.auth.common.exception.UnauthorizedException;
import com.saborclick.auth.common.security.jwt.TokenBlacklistService;
import com.saborclick.auth.dto.ErrorResponse;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ⛔ Evita validar token en rutas públicas
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseAllClaims(token);
            String userId = claims.get("id", String.class);
            String rol = claims.get("rol", String.class);

            List<SimpleGrantedAuthority> authorities = (rol != null)
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                    : Collections.emptyList();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Usuario no válido"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (tokenBlacklistService.isTokenRevoked(token)) {
                throw new UnauthorizedException("Token inválido o expirado");
            }

        } catch (Exception e) {
            log.warn("Token JWT inválido o expirado: {}", e.getMessage());

            String traceId = MDC.get("traceId");

            ErrorResponse error = new ErrorResponse("🚫 Token inválido o expirado", 401, traceId);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(error);

            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/solicitar-reset")
                || path.equals("/api/auth/restablecer-password")
                || path.equals("/api/auth/ping");
    }
}
