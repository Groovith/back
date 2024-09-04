package com.groovith.groovith.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNicknameRequestDto {
    @NotBlank
    private String nickname;
}
