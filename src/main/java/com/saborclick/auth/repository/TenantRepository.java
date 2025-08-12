// Paquete sugerido: com.saborclick.auth.repository

package com.saborclick.auth.repository;

import com.saborclick.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByEmail(String email);
    boolean existsByEmail(String email);
}