package com.saborclick.auth.controller;

import com.saborclick.auth.common.security.annotations.CurrentSessionHash;
import com.saborclick.auth.common.security.annotations.CurrentUserId;
import com.saborclick.auth.dto.SuccessResponse;
import com.saborclick.auth.dto.users.*;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.mapper.UserMapper;
import com.saborclick.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<SuccessResponse> createUser(
            @RequestBody @Valid CreateUserRequest request,
            @CurrentUserId String userId
    ) {
        userService.createUser(request);
        return ResponseEntity.ok(
            SuccessResponse.builder()
            .success(true)
                        .message("✅ Tu tenant ha sido creado exitosamente. Revisa tu correo para activar la cuenta.")
                        .build()
        );
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<SuccessResponse> updateUser(@RequestBody @Valid UpdateUserRequest request,
                                                      @CurrentSessionHash String sessionHash) {
        userService.updateUserData(request, sessionHash);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Datos actualizados correctamente")
                .success(true)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<List<UserResponse>> getUsers(
            @CurrentSessionHash String sessionHash
    ) {
        List<User> users = userService.findUsersInMyTenant();
        List<UserResponse> response = users.stream()
                .map(user -> userMapper.toResponse(user, sessionHash))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<SuccessResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                                             @CurrentSessionHash String sessionHash) {
        userService.updatePassword(request.getUserId(), request.getNewPassword(), sessionHash);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Contraseña actualizada correctamente")
                .success(true)
                .build());
    }

    @PostMapping("/update-status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<SuccessResponse> updateStatus(@Valid @RequestBody UpdateStatusRequest request,
                                                           @CurrentSessionHash String sessionHash) {
        userService.updateStatus(request.getUserId(), request.isEnable(), sessionHash);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Estado actualizado correctamente")
                .success(true)
                .build());
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<SuccessResponse> deleteUser(@RequestBody @Valid DeleteUserRequest request,
                                                         @CurrentSessionHash String sessionHash) {
        userService.deleteUser(request.getUserId(), sessionHash, request.getReason());
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Usuario eliminado correctamente")
                .success(true)
                .build());
    }
}
