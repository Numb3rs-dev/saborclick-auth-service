package com.saborclick.auth.dto.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String id; // secureId
    private String userName;
    private String email;
    private String name;
    private String lastName;
    private String phone;
    private String mobilePhone;

    private String country;
    private String city;
    private String address;
    private String addressLine2;
    private Double lat;
    private Double lon;
    private String zone;

    private String rol;
    private boolean isActive;
}
