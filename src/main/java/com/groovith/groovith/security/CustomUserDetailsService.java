package com.groovith.groovith.security;

import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
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
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() ->new UsernameNotFoundException("Username not found."));
        return new CustomUserDetails(user);
    }
}