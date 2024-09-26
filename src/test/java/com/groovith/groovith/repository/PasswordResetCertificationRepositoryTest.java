package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Certification;
import com.groovith.groovith.domain.PasswordResetCertification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PasswordResetCertificationRepositoryTest {
    @Autowired
    PasswordResetCertificationRepository passwordResetCertificationRepository;

    @AfterEach
    void deleteAll(){
        passwordResetCertificationRepository.deleteAll();
    }

    @Test
    public void save(){
        //given
        String email = "test1@gmail.com";
        PasswordResetCertification data = new PasswordResetCertification(email, "code");

        //when
        PasswordResetCertification certification = passwordResetCertificationRepository.save(data);

        //then
        Assertions.assertThat(data).isEqualTo(certification);
        Assertions.assertThat(certification.getEmail()).isEqualTo(data.getEmail());
        Assertions.assertThat(certification.getCode()).isEqualTo(data.getCode());
    }

    @Test
    public void findById(){
        //given
        String email = "test1@gmail.com";
        PasswordResetCertification data = new PasswordResetCertification(email, "code");

        //when
        passwordResetCertificationRepository.save(data);
        PasswordResetCertification findCertification = passwordResetCertificationRepository.findById(email)
                .orElseThrow(()-> new IllegalArgumentException("email이 없음"+email));

        //then
        Assertions.assertThat(findCertification.getEmail()).isEqualTo(email);
        Assertions.assertThat(findCertification.getCode()).isEqualTo("code");
    }

    @Test
    @DisplayName("만료시간 이후에 삭제되는지 테스트")
    public void ttlTest() throws Exception{
        //given
        String email = "test1@gmail.com";
        PasswordResetCertification data = new PasswordResetCertification(email, "code");
        //when
        passwordResetCertificationRepository.save(data);
        Thread.sleep(5000); // ttl 설정 5초 일때
        Optional<PasswordResetCertification> certification = passwordResetCertificationRepository.findById(email);

        //then
        // 유효시간 지난 후에 데이터가 삭제되었는지 테스트
        Assertions.assertThat(certification).isEqualTo(Optional.empty());
    }

}