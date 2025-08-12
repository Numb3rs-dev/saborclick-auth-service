package com.saborclick.auth.mail;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destino;
    private String asunto;

    @Column(length = 2000)
    private String cuerpo;

    private LocalDateTime fechaEnvio;

    private boolean enviado;

    private String error; // null si todo bien
}
