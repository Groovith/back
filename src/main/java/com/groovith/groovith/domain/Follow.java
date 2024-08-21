package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id")
    private User following;

    @CreationTimestamp
    private LocalDateTime followedAt;

    // 팔로우 요청을 받았는지(or 삭제했는지)/ 안받았는지
    private FollowStatus status;    // ACCEPTED, PENDING, REJECTED

    public void updateStatus(FollowStatus status){
        this.status = status;
    }
}
