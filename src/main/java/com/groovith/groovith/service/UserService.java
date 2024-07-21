package com.groovith.groovith.service;

import com.groovith.groovith.repository.RefreshRepository;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.dto.UserDetailsResponse;
import com.groovith.groovith.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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

        User data = new User();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        // data.setRole("ROLE_USER"); 롤은 현재 사용하지 않음

        userRepository.save(data);
    }

    public ResponseEntity<UserDetailsResponse> getCurrentUserDetails(String accessToken) {

//        String username = jwtUtil.getUsername(accessToken);
//        UserEntity user = userRepository.findByUsername(username).orElse(null);
//        if (user == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
        Long userId = jwtUtil.getUserId(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("유저가 존재하지 않습니다. userId:"+userId));
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(userId, user.getUsername());


        return new ResponseEntity<>(userDetailsResponse, HttpStatus.OK);
    }
}
