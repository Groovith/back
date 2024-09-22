package com.groovith.groovith.domain;

import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "certification", timeToLive = 300)    // 5분
public class Certification{
    @Id
    private String email;
    private String certificationNumber;
    private boolean isCertificated;
}
