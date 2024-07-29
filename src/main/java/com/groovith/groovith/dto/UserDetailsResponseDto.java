package com.groovith.groovith.dto;

import com.groovith.groovith.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private Long id;
    private String username;

    public UserDetailsResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}
