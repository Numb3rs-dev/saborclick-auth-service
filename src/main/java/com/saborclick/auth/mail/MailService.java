package com.saborclick.auth.mail;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailLogRepository mailLogRepository;

    public void enviar(String destino, String asunto, String cuerpo) {
        MailLog logEntry = MailLog.builder()
                .destino(destino)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .fechaEnvio(LocalDateTime.now())
                .enviado(false)
                .build();

        try {
            log.info("✉️ Enviando correo a {}", destino);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("notificaciones@saborclick.com"); // Debe coincidir con tu usuario SMTP
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(cuerpo, false); // false = texto plano, true = HTML

            mailSender.send(message);
            log.info("✅ Correo enviado a {}", destino);

            logEntry.setEnviado(true);

        } catch (MailException e) {
            log.error("❌ Error al enviar correo a {}: {}", destino, e.getMessage(), e);
            logEntry.setError(e.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar correo", e);
            logEntry.setError(e.getMessage());
        }

        mailLogRepository.save(logEntry);
    }
}
