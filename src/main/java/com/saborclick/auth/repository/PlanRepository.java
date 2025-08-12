package com.saborclick.auth.repository;

import com.saborclick.auth.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, String> {

    Optional<Plan> findByName(String nombre);

    List<Plan> findByActiveTrue();

    // 🔥 Nuevo: busca el plan activo con menor nivel
    Optional<Plan> findFirstByActiveTrueOrderByLevelAsc();

    // 🔥 Plan activo con mayor nivel (plan premium)
    Optional<Plan> findFirstByActiveTrueOrderByLevelDesc();
}
