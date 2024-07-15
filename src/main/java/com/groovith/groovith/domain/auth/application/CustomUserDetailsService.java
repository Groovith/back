package com.groovith.groovith.domain.auth.application;

import com.groovith.groovith.domain.auth.dto.CustomUserDetails;
import com.groovith.groovith.domain.user.dao.UserRepository;
import com.groovith.groovith.domain.user.domain.UserEntity;
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