package com.groovith.groovith.service;

import com.groovith.groovith.dto.CustomUserDetails;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userData = userRepository.findByUsername(username).orElse(null);

        if (userData != null) {
            return new CustomUserDetails(userData);
        }


        return null;
    }
}