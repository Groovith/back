package com.groovith.groovith.domain;

import com.groovith.groovith.domain.enums.FollowStatus;
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

    @Enumerated(EnumType.STRING)
    private FollowStatus status;    // NOFOLLOW, ACCEPTED, PENDING, REJECTED

    public void updateStatus(FollowStatus status){
        this.status = status;
    }
}
