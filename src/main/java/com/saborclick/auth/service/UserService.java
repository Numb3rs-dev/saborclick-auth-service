package com.saborclick.auth.service;

import com.saborclick.auth.common.exception.ConflictException;
import com.saborclick.auth.common.exception.ForbiddenException;
import com.saborclick.auth.common.exception.NotFoundException;
import com.saborclick.auth.common.exception.UnauthorizedException;
import com.saborclick.auth.common.security.SecureIdService;
import com.saborclick.auth.dto.users.CreateUserRequest;
import com.saborclick.auth.dto.users.UpdateUserRequest;
import com.saborclick.auth.entity.Tenant;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.enums.Rol;
import com.saborclick.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureIdService secureIdService;

    public void activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.isActive()) {
            throw new ConflictException("Usuario ya está activado");
        }

        user.setActive(true);
        userRepository.save(user);
    }

    public User createUser(CreateUserRequest request) {
        User actual = getCurrentUser();

        // Validaciones de unicidad
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new ConflictException("Nombre de usuario ya está en uso");
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Correo electrónico ya está registrado");
        }

        // ✅ Validar cambio de rol permitido
        if (request.getRol() != null) {
            if (!Set.of("TENANT_USER", "TENANT_ADMIN").contains(request.getRol())) {
                throw new IllegalArgumentException("Rol inválido: solo se permite TENANT_USER o TENANT_ADMIN");
            }
        }

        Tenant tenant = actual.getTenant();

        User nuevo = User.builder()
                .id("usr_" + UUID.randomUUID())
                .userName(request.getUserName())
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.valueOf(request.getRol()))
                .tenant(tenant)
                .isActive(true)
                .isDeleted(false)
                .build();

        log.info("Usuario {} creado por {}", request.getUserName(), actual.getUsername());
        return userRepository.save(nuevo);
    }

    public void updateUserData(UpdateUserRequest request, String sessionHash) {
        User currentUser = getCurrentUser();

        // 🛡️ Validar y extraer el ID real desde el hash
        String rawId = secureIdService.verifyAndExtract(request.getUserId(), sessionHash);

        User targetUser = userRepository.findById(rawId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 🔐 Verifica que el currentUser tenga permisos para modificar
        if (!canModify(currentUser, targetUser)) {
            throw new ForbiddenException("No autorizado para modificar este usuario");
        }

        // ✅ Validar cambio de rol permitido
        if (request.getRol() != null) {
            if (!Set.of("TENANT_USER", "TENANT_ADMIN").contains(request.getRol())) {
                throw new IllegalArgumentException("Rol inválido: solo se permite TENANT_USER o TENANT_ADMIN");
            }
        }

        // ✏️ Actualiza los datos permitidos
        targetUser.setName(request.getName());
        targetUser.setLastName(request.getLastName());
        targetUser.setPhone(request.getPhone());
        targetUser.setMobilePhone(request.getMobilePhone());
        targetUser.setCountry(request.getCountry());
        targetUser.setCity(request.getCity());
        targetUser.setAddress(request.getAddress());
        targetUser.setAddressLine2(request.getAddressLine2());
        targetUser.setLat(request.getLat());
        targetUser.setLon(request.getLon());
        targetUser.setZone(request.getZone());

        userRepository.save(targetUser);
    }


    public void updatePassword(String secureUserId, String newPassword, String sessionHash) {
        User current = getCurrentUser();
        String userId = secureIdService.verifyAndExtract(secureUserId, sessionHash);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!canModify(current, target)) {
            throw new ForbiddenException("No tienes permisos para cambiar esta contraseña");
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        log.info("🔐 Contraseña actualizada para usuario {} por {}", target.getEmail(), current.getEmail());
    }

    public void updateStatus(String secureUserId, boolean enable, String sessionHash) {
        User current = getCurrentUser();
        String userId = secureIdService.verifyAndExtract(secureUserId, sessionHash);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!canModify(current, target)) {
            throw new ForbiddenException("No tienes permisos para modificar este usuario");
        }

        target.setActive(enable);
        userRepository.save(target);
        log.info("🔁 Estado actualizado para usuario {}: {}", target.getEmail(), enable ? "Activo" : "Inactivo");
    }

    private boolean canModify(User currentUser, User targetUser) {
        return currentUser.getRol() == Rol.SUPER_ADMIN
                || (currentUser.getRol() == Rol.TENANT_OWNER
                && currentUser.getTenant().getId().equals(targetUser.getTenant().getId()));
    }

    public void deleteUser(String secureId, String sessionHash, String reason) {
        User currentUser = getCurrentUser();

        String rawId = secureIdService.verifyAndExtract(secureId, sessionHash);
        User targetUser = userRepository.findById(rawId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!canModify(currentUser, targetUser)) {
            throw new ForbiddenException("No autorizado para eliminar este usuario");
        }

        // Marcar como eliminado
        targetUser.setActive(false);
        targetUser.setDeleted(true);
        targetUser.setDeletedReason(reason);

        // Liberar username y email
        String suffix = "_deleted_" + UUID.randomUUID().toString().substring(0, 8);
        targetUser.setUserName(targetUser.getUsername() + suffix);
        if (targetUser.getEmail() != null) {
            targetUser.setEmail(targetUser.getEmail() + suffix);
        }

        userRepository.save(targetUser);
        log.info("🗑️ Usuario {} eliminado lógicamente", targetUser.getId());
    }


    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated() || !(auth.getPrincipal() instanceof User)) {
            throw new UnauthorizedException("Usuario no autenticado o inválido");
        }
        return (User) auth.getPrincipal();
    }

    public List<User> findUsersInMyTenant() {
        User currentUser = getCurrentUser();
        String tenantId = currentUser.getTenant().getId();

        return userRepository.findAllByTenantIdAndAndIsDeletedFalse(tenantId);
    }

}
