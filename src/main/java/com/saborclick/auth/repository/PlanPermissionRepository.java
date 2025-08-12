package com.saborclick.auth.repository;

import com.saborclick.auth.entity.Plan;
import com.saborclick.auth.entity.PlanPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanPermissionRepository extends JpaRepository<PlanPermission, String> {

    boolean existsByPlanAndKey(Plan plan, String key);

    List<PlanPermission> findByPlan(Plan plan);

    List<PlanPermission> findByPlanId(String planId);

    List<PlanPermission> findByPlan_Name(String planName); // Útil si quieres buscar por nombre de plan directamente

    PlanPermission findByPlanAndKey(Plan plan, String key); // Si quieres obtener directamente un permiso específico
}
