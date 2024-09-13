package com.groovith.groovith.domain;

import com.groovith.groovith.dto.UserChatRoomDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="user")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String nickname;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "streaming", nullable = false)
    @Enumerated(EnumType.STRING)
    private StreamingType streaming;

    @Column(name = "spotify_refresh_token")
    private String spotifyRefreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Refresh> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE)
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.REMOVE)
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserChatRoom> userChatRoom = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();
    public UserChatRoomDto toUserChatRoomDto(User user){
        return new UserChatRoomDto(user);
    }

    public void updateStatus(UserStatus userStatus){
        this.status = (userStatus==UserStatus.PUBLIC) ? UserStatus.PRIVATE : UserStatus.PUBLIC;
    }
}
