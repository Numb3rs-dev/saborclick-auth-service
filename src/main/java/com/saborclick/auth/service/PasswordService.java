package com.saborclick.auth.service;

import com.saborclick.auth.dto.auth.NewPasswordRequest;
import com.saborclick.auth.entity.PasswordResetToken;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.mail.MailService;
import com.saborclick.auth.repository.PasswordResetTokenRepository;
import com.saborclick.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public void sendTokenToRecovery(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiration(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        String link = "https://tuapp.com/restablecer-password?token=" + token;

        String mensaje = """
                Hola %s,

                Has solicitado restablecer tu contraseña. Usa el siguiente enlace:

                %s

                Este enlace expira en 30 minutos.
                """.formatted(user.getName(), link);
        mailService.enviar(email, "🔐 Recuperar contraseña", mensaje);
    }

    public void updatePasswordWithToken(NewPasswordRequest request) {
        PasswordResetToken reset = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (reset.isUsed() || reset.getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado o ya utilizado");
        }

        User user = reset.getUser();

        // 🔐 Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 🔓 Desbloquear usuario si estaba bloqueado
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setActive(true);
        user.setLockUntil(null);

        userRepository.save(user);

        // ✅ Marcar token como usado
        reset.setUsed(true);
        tokenRepository.save(reset);

        log.info("🔒 Contraseña restablecida y cuenta desbloqueada para el usuario: {}", user.getEmail());
    }
}
