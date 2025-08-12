package com.saborclick.auth.service;

import com.saborclick.auth.dto.PlanRequest;
import com.saborclick.auth.entity.Plan;
import com.saborclick.auth.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public List<Plan> planList() {
        return planRepository.findAll();
    }

    public List<Plan> listarActivos() {
        return planRepository.findByActiveTrue();
    }

    public Plan crear(PlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .level(request.getLevel())
                .active(true)
                .build();
        return planRepository.save(plan);
    }

    public Plan update(String id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setLevel(request.getLevel());
        plan.setActive(request.isActive());

        return planRepository.save(plan);
    }

    public void eliminar(String id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado"));

        planRepository.delete(plan);
    }
}
