package com.saborclick.auth.service;

import com.saborclick.auth.common.exception.ConflictException;
import com.saborclick.auth.common.security.JwtService;
import com.saborclick.auth.common.security.SecureIdService;
import com.saborclick.auth.dto.ChangePlanRequest;
import com.saborclick.auth.dto.tenants.TenantSignupRequest;
import com.saborclick.auth.dto.tenants.TenantUpdateRequest;
import com.saborclick.auth.entity.Plan;
import com.saborclick.auth.entity.Tenant;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.enums.Rol;
import com.saborclick.auth.mail.MailService;
import com.saborclick.auth.repository.PlanRepository;
import com.saborclick.auth.repository.TenantRepository;
import com.saborclick.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    //private final CaptchaValidator captchaValidator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureIdService secureIdService;
    private final JwtService jwtService;

    public Tenant changePlan(ChangePlanRequest request) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant no encontrado"));

        Plan nuevoPlan = planRepository.findById(request.getNuevoPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        tenant.setPlan(nuevoPlan);
        return tenantRepository.save(tenant);
    }

    public Tenant registerTenantWithAdmin(TenantSignupRequest request, String sessionHash) {
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe un tenant con este correo");
        }

        if (userRepository.findByUserName(request.getEmail()).isPresent()) {
            throw new ConflictException("El nombre de usuario ya está en uso");
        }

        // Crear Tenant con plan básico
        Plan plan = planRepository.findFirstByActiveTrueOrderByLevelAsc()
                .orElseThrow(() -> new IllegalStateException("No hay planes activos disponibles"));

        Tenant tenant = Tenant.builder()
                .id("tnt_" + UUID.randomUUID())
                .name(request.getTenantName())
                .email(request.getEmail())
                .plan(plan)
                .build();
        tenantRepository.save(tenant);

        // Crear usuario administrador
        User user = User.builder()
                .id("tnt_" + UUID.randomUUID())
                .userName(request.getEmail())
                .email(request.getEmail())
                .name(request.getName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobilePhone(request.getMobilePhone())
                .country(request.getCountry())
                .city(request.getCity())
                .rol(Rol.TENANT_OWNER)
                .tenant(tenant)
                .isActive(false)
                .build();
        userRepository.save(user);

        // Enviar correo de activación
        String activationToken = jwtService.generateActivationToken(user);
        String activationLink = "https://tusistema.com/api/auth/activate?token=" + activationToken;
        String mensaje = """
            Hola %s,

            Tu tenant fue creado exitosamente 🎉

            Para activar tu usuario, usa el siguiente enlace:
            %s

            Este enlace expira en 30 minutos.
            """.formatted(user.getName(), activationLink);

        mailService.enviar(user.getEmail(), "🔐 Activación de cuenta", mensaje);

        log.info("Tenant creado con éxito: {} - Usuario administrador: {}", tenant.getName(), user.getEmail());

        return tenant;
    }


    public void updateCurrentTenantProfile(TenantUpdateRequest request, String currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Tenant tenant = user.getTenant();
        tenant.setName(request.getName());
        tenantRepository.save(tenant);

        // Update owner user profile (location/contact info)
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAddressLine2(request.getAddressLine2());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setLat(request.getLat());
        user.setLon(request.getLon());
        user.setZone(request.getZone());

        userRepository.save(user);
    }


}
