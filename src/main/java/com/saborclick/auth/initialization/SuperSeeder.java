package com.saborclick.auth.initialization;

import com.saborclick.auth.entity.Tenant;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.enums.Rol;
import com.saborclick.auth.repository.PlanRepository;
import com.saborclick.auth.repository.TenantRepository;
import com.saborclick.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SuperSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;

    @Value("${app.bootstrap-superadmin.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap-superadmin.username:admin}")
    private String username;

    @Value("${app.bootstrap-superadmin.email:admin@saborclick.com}")
    private String email;

    @Value("${app.bootstrap-superadmin.name:Super Admin}")
    private String name;

    @Value("${app.bootstrap-superadmin.password:admin123}")
    private String rawPassword;

    @Value("${app.bootstrap-superadmin.tenant-name:TenantGlobal}")
    private String tenantName;

    @Value("${app.bootstrap-superadmin.tenant-id:tnt_global}")
    private String tenantId;

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) {
            log.info("🔒 Bootstrap deshabilitado (app.bootstrap-superadmin.enabled=false)");
            return;
        }

        boolean exists = userRepository.findByRol(Rol.SUPER_ADMIN).isPresent();
        if (exists) {
            log.info("🧍 Usuario SUPER_ADMIN ya existe. No se creó ninguno.");
            return;
        }

        // ✅ Crear Tenant base si no existe
        Tenant tenant = tenantRepository.findById(tenantId).orElseGet(() -> {
            Tenant nuevo = Tenant.builder()
                    .id(tenantId)
                    .name(tenantName)
                    .email("global@saborclick.com")
                    .plan(planRepository.findFirstByActiveTrueOrderByLevelAsc()
                            .orElseThrow(() -> new IllegalStateException("No hay planes activos disponibles")))
                    .build();
            log.info("🏢 Tenant global creado: {}", tenantName);
            return tenantRepository.save(nuevo);
        });

        // ✅ Crear Super Admin
        User superAdmin = User.builder()
                .id("usr_superadmin")
                .userName(username)
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(rawPassword))
                .rol(Rol.SUPER_ADMIN)
                .isActive(true)
                .isDeleted(false)
                .tenant(tenant)
                .build();

        userRepository.save(superAdmin);
        log.warn("🛡️ Usuario SUPER_ADMIN creado automáticamente.");
        log.warn("➡️  Username: {}", username);
        log.warn("📧  Email: {}", email);
        log.warn("🔑  Contraseña: {}", rawPassword);
    }
}
