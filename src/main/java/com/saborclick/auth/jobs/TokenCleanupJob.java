package com.saborclick.auth.jobs;

import com.saborclick.auth.common.config.TokenCleanupProperties;
import com.saborclick.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupJob implements SchedulingConfigurer {

    private final PasswordResetTokenRepository tokenRepository;
    private final TokenCleanupProperties properties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Runnable task = () -> {
            LocalDateTime ahora = LocalDateTime.now();
            int eliminados = tokenRepository.deleteByUsedTrueOrExpirationBefore(ahora);
            if (eliminados > 0) {
                log.info("🧹 Tokens de recuperación eliminados: {}", eliminados);
            }
        };

        // ✅ Usamos Instant porque así lo requiere Spring Framework 6+
        taskRegistrar.addTriggerTask(task, context ->
                new CronTrigger(properties.getCleanupCron()).nextExecution(context));
    }
}
