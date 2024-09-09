package com.groovith.groovith.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String password;
}
