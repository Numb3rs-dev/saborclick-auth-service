package com.saborclick.auth.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/correos/logs")
@RequiredArgsConstructor
public class MailLogController {

    private final MailLogRepository mailLogRepository;

    @GetMapping
    public List<MailLog> listar() {
        return mailLogRepository.findAll();
    }
}
