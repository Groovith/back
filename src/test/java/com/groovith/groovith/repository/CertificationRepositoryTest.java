package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Certification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CertificationRepositoryTest {

    @Autowired CertificationRepository certificationRepository;

    @BeforeEach
    void deleteAll(){
        certificationRepository.deleteAll();
    }

    @Test
    @DisplayName("redis 저장 테스트")
    public void save(){
        //given
        String email = "test1@gmail.com";
        Certification data = new Certification(email, "1234", false);

        //when
        Certification certification = certificationRepository.save(data);

        //then
        Assertions.assertThat(data).isEqualTo(certification);
        Assertions.assertThat(data.getEmail()).isEqualTo(certification.getEmail());
        Assertions.assertThat(data.getCertificationNumber())
                .isEqualTo(certification.getCertificationNumber());
        Assertions.assertThat(certification.isCertificated())
                .isEqualTo(false);
    }

    @Test
    @DisplayName("조회 By email 테스트")
    public void findById(){
        //given
        String email = "test1@gmail.com";
        String num = "1234";
        Certification data = new Certification(email, num, false);
        //when
        certificationRepository.save(data);
        Certification findCertification = certificationRepository.findById(email)
                .orElseThrow(()->new IllegalArgumentException("이메일 없음 : "+email));
        //then
        Assertions.assertThat(findCertification.getEmail()).isEqualTo(email);
        Assertions.assertThat(findCertification.getCertificationNumber())
                .isEqualTo(num);
        Assertions.assertThat(findCertification.isCertificated())
                .isEqualTo(false);
    }

    @Test
    @DisplayName("지정한 만료시간 후에 데이터가 삭제되는지 테스트")
    public void ttlTest() throws Exception{
        //given
        String email = "test1@gmail.com";
        String num = "1234";
        Certification data = new Certification(email, num, false);
        //when
        certificationRepository.save(data);
        Thread.sleep(5000); // 테스트 시에 Certification의 ttl 5초로 설정해둠
        Optional<Certification> certification = certificationRepository.findById(email);

        //then
        // 유효기간 지난 후에 조회시에 데이터가 삭제되서 없어야함
        Assertions.assertThat(certification).isEqualTo(Optional.empty());
    }
}
