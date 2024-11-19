package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Entity
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 추가한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    // 친구 추가 받은 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Friend(User fromUser, User toUser){
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public static Friend setFriend(User fromUser, User toUser){
        Friend friend = Friend.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
        fromUser.getFriends().add(friend);
        return friend;
    }

    public static void deleteFriend(User fromUser, User toUser){
        fromUser.getFriends().removeIf(friend -> friend.getToUser().equals(toUser));
    }
}
