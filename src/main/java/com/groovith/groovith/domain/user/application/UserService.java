package com.groovith.groovith.domain.user.application;

import com.groovith.groovith.domain.auth.dao.RefreshRepository;
import com.groovith.groovith.domain.user.dao.UserRepository;
import com.groovith.groovith.domain.user.domain.UserEntity;
import com.groovith.groovith.domain.user.dto.JoinDto;
import com.groovith.groovith.domain.user.dto.UserDetailsResponse;
import com.groovith.groovith.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public void join(JoinDto joinDto) throws IllegalArgumentException {
        String username = joinDto.getUsername();
        String password = joinDto.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            throw new IllegalArgumentException("User with given username already exists");
        }

        UserEntity data = new UserEntity();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_ADMIN");

        userRepository.save(data);
    }

    public ResponseEntity<UserDetailsResponse> getCurrentUserDetails(String accessToken) {

        String username = jwtUtil.getUsername(accessToken);
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(user.getId(), user.getUsername());

        return new ResponseEntity<>(userDetailsResponse, HttpStatus.OK);
    }
}
