package com.groovith.groovith.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetEmailRequestDto {
    @NotBlank
    @Email
    private String email;
}
