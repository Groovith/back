package com.groovith.groovith.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinRequestDto {

    private String username;
    private String password;
    @Email
    private String email;
}
