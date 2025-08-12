// -------------------------
// Entidad Plan con jerarquía
// -------------------------
package com.saborclick.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Plan extends AuditableEntity {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer level; // entre más alto, más capacidades

    private boolean active;

    public boolean esSuperiorA(Plan otro) {
        return this.level != null && otro != null && this.level > otro.level;
    }

    public boolean esIgualOInferiorA(Plan otro) {
        return this.level != null && otro != null && this.level <= otro.level;
    }
}