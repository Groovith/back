package com.groovith.groovith.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckCertificationRequestDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String certificationNumber;
}
