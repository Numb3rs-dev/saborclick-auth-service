// -------------------------
// Entidad Tenant
// -------------------------
package com.saborclick.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenants")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Tenant extends AuditableEntity {

    @Id
    @Column(length = 40, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}