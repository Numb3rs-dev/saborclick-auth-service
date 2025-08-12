package com.saborclick.auth.controller;

import com.saborclick.auth.common.security.annotations.SecureId;
import com.saborclick.auth.dto.PlanRequest;
import com.saborclick.auth.service.PlanService;
import com.saborclick.auth.entity.Plan;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<Plan> listPlan() {
        return planService.planList();
    }

    @GetMapping("/activos")
    public List<Plan> listarActivos() {
        return planService.listarActivos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Plan crear(@RequestBody PlanRequest request) {
        return planService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Plan actualizar(@SecureId @PathVariable("id") String id, @RequestBody PlanRequest request) {
        return planService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void eliminar(@SecureId @PathVariable("id") String id) {
        planService.eliminar(id);
    }
}