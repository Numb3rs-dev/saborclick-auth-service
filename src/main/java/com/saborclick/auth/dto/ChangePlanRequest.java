package com.saborclick.auth.dto;

import lombok.Data;

@Data
public class ChangePlanRequest {
    private String tenantId;
    private String nuevoPlanId;
}