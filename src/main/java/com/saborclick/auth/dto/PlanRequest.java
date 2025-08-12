package com.saborclick.auth.dto;

import lombok.Data;

@Data
public class PlanRequest {
    private String name;
    private String description;
    private int level;
    private boolean active;
}
