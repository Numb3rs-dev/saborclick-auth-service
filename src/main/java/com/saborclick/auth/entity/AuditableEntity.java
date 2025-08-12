// Proyecto: auth-service (modelo limpio sin subpaquete 'model')
// Reglas aplicadas:
// - Eliminado 'model.' en los paquetes
// - Solo un administrador puede crear tenants
// - Solo un tenant puede crear usuarios dentro de su contexto

// -------------------------
// Base de auditoría
// -------------------------
package com.saborclick.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class AuditableEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Column
    private String createdFromIp;

    @Column
    private String lastModifiedFromIp;
}