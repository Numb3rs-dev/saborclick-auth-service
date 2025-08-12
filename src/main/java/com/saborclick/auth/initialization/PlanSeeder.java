package com.saborclick.auth.initialization;

import com.saborclick.auth.entity.Plan;
import com.saborclick.auth.entity.PlanPermission;
import com.saborclick.auth.repository.PlanPermissionRepository;
import com.saborclick.auth.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class PlanSeeder implements ApplicationRunner {

    private final PlanRepository planRepository;
    private final PlanPermissionRepository permissionRepository;

    private final Map<String, Plan> createdPlans = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        log.info("🚀 Inicializando planes y permisos...");

        seedPlans();
        seedPermissions();

        log.info("✅ Planes y permisos iniciales cargados.");
    }

    private void seedPlans() {
        createIfNotExists("Degustación", 100, "Plan gratuito para restaurantes pequeños.", true);
        createIfNotExists("Menú Ejecutivo", 200, "Plan para negocios en expansión.", true);
        createIfNotExists("Carta Completa", 300, "Todo el poder de la plataforma, reservas y estadísticas.", true);
        createIfNotExists("Chef Estrella", 400, "Funcionalidades premium, marca blanca, soporte dedicado.", true);
    }

    private void createIfNotExists(String name, int level, String description, boolean active) {
        planRepository.findByName(name).ifPresentOrElse(
                existing -> {
                    log.info("🔎 Plan ya existe: {}", name);
                    createdPlans.put(name, existing);
                },
                () -> {
                    Plan plan = Plan.builder()
                            .id(UUID.randomUUID().toString())
                            .name(name)
                            .level(level)
                            .description(description)
                            .active(active)
                            .build();
                    Plan saved = planRepository.save(plan);
                    createdPlans.put(name, saved);
                    log.info("📦 Plan creado: {} (nivel {})", name, level);
                }
        );
    }

    private void seedPermissions() {
        Map<String, List<PlanPermission>> permissionsByPlan = Map.of(
                "Degustación", List.of(
                        permission("restaurant", "max_restaurants", "1"),
                        permission("restaurant", "max_branches_per_restaurant", "1"),
                        permission("restaurant", "max_tables_per_branch", "5"),
                        permission("orders", "enable_reservations", "false"),
                        permission("reports", "enable_statistics", "false")
                ),
                "Menú Ejecutivo", List.of(
                        permission("restaurant", "max_restaurants", "3"),
                        permission("restaurant", "max_branches_per_restaurant", "2"),
                        permission("restaurant", "max_tables_per_branch", "15"),
                        permission("orders", "enable_reservations", "true"),
                        permission("reports", "enable_statistics", "false")
                ),
                "Carta Completa", List.of(
                        permission("restaurant", "max_restaurants", "10"),
                        permission("restaurant", "max_branches_per_restaurant", "5"),
                        permission("restaurant", "max_tables_per_branch", "50"),
                        permission("orders", "enable_reservations", "true"),
                        permission("reports", "enable_statistics", "true")
                ),
                "Chef Estrella", List.of(
                        permission("restaurant", "max_restaurants", "unlimited"),
                        permission("restaurant", "max_branches_per_restaurant", "unlimited"),
                        permission("restaurant", "max_tables_per_branch", "unlimited"),
                        permission("orders", "enable_reservations", "true"),
                        permission("reports", "enable_statistics", "true")
                )
        );

        permissionsByPlan.forEach((planName, permissions) -> {
            Plan plan = createdPlans.get(planName);
            if (plan == null) {
                log.warn("⚠️ No se encontró el plan '{}', no se asignaron permisos.", planName);
                return;
            }

            for (PlanPermission perm : permissions) {
                boolean exists = permissionRepository.existsByPlanAndKey(plan, perm.getKey());
                if (!exists) {
                    perm.setId(UUID.randomUUID().toString());
                    perm.setPlan(plan);
                    permissionRepository.save(perm);
                    log.info("🔑 Permiso '{}' agregado al plan '{}'", perm.getKey(), planName);
                }
            }
        });
    }

    private PlanPermission permission(String module, String key, String value) {
        return PlanPermission.builder()
                .module(module)
                .key(key)
                .value(value)
                .build();
    }
}
