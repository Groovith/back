package com.groovith.groovith.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

//@Entity
@Getter
@AllArgsConstructor
@RedisHash(value = "refresh", timeToLive = 604800) // 일주일
public class Refresh {

    @Id
    private String refresh;

    private Long userId;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "refresh")
//    private String refresh;
//
//    @Column(name = "expiration")
//    private String expiration;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
}