package com.groovith.groovith.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameCheckRequestDto {
    @NotBlank
    private String username;
}
