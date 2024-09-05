package com.groovith.groovith.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUsernameRequestDto {
    @NotBlank
    private String username;
}
