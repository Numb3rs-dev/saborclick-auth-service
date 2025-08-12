package com.saborclick.auth.entity;

import com.saborclick.auth.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User extends AuditableEntity implements UserDetails {

    @Id
    @Column(length = 40, nullable = false, updatable = false)
    private String id;

    @Column(unique = true, nullable = false)
    private String userName; // Nuevo campo para identificador de login

    @Column(unique = true)
    private String email; // Opcional

    private String name;
    private String lastName;

    private String password;

    private String phone;
    private String mobilePhone;

    //Location
    private String country;
    private String city;
    private String address;
    private String addressLine2;
    private Double lat;
    private Double lon;
    private String zone;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    private boolean isActive = true;

    private int failedLoginAttempts;
    private boolean accountLocked;
    private Date lockUntil;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_reason")
    private String deletedReason;

    // 🔐 Rol para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    // 🔐 Username (login)
    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}