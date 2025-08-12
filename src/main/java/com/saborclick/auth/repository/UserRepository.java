// Paquete sugerido: com.saborclick.auth.repository

package com.saborclick.auth.repository;

import com.saborclick.auth.entity.User;
import com.saborclick.auth.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByRol(Rol rol);
    List<User> findAllByTenantId(String tenantId);
    List<User> findAllByTenantIdAndAndIsDeletedFalse(String tenantId);
}
