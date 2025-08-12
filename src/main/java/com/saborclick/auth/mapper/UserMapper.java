package com.saborclick.auth.mapper;

import com.saborclick.auth.dto.users.UserResponse;
import com.saborclick.auth.entity.User;
import com.saborclick.auth.common.security.SecureIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final SecureIdService secureIdService;

    public UserResponse toResponse(User user, String sessionHash) {
        return UserResponse.builder()
                .id(secureIdService.generateSecureId(user.getId(), sessionHash))
                .userName(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .mobilePhone(user.getMobilePhone())
                .country(user.getCountry())
                .city(user.getCity())
                .address(user.getAddress())
                .addressLine2(user.getAddressLine2())
                .lat(user.getLat())
                .lon(user.getLon())
                .zone(user.getZone())
                .rol(user.getRol().name())
                .isActive(user.isActive())
                .build();
    }
}