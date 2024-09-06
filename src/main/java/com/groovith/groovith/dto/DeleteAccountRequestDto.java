package com.groovith.groovith.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDto {
    @NotBlank
    private String password;
}
