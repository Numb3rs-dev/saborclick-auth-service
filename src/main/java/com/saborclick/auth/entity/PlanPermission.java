package com.saborclick.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_permissions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanPermission {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String key; // Ej: max_restaurants, enable_reservations

    @Column(nullable = false)
    private String value; // Ej: "3", "true", "false"

    @Column(nullable = false)
    private String module; // Ej: "restaurant", "orders", "billing", "reports"

    public boolean isEnabled() {
        return "true".equalsIgnoreCase(value);
    }

    public Integer asInteger() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
