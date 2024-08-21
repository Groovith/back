package com.groovith.groovith.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailCertificationRequestDto {
    @NotBlank
    @Email
    private String email;
}
