package com.groovith.groovith.domain;

import com.groovith.groovith.dto.UserChatRoomDto;
import jakarta.persistence.*;
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

    @Column(name = "streaming", nullable = false)
    @Enumerated(EnumType.STRING)
    private StreamingType streaming;

    @Column(name = "spotify_refresh_token")
    private String spotifyRefreshToken;

    @OneToMany(mappedBy = "follower")
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following")
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<UserChatRoom> userChatRoom = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();

    public UserChatRoomDto toUserChatRoomDto(User user){
        return new UserChatRoomDto(user);
    }

    public void updateStatus(UserStatus userStatus){
        System.out.println("USERSTATE : "+userStatus + " " + this.getStatus());
        this.status = (userStatus==UserStatus.PUBLIC) ? UserStatus.PRIVATE : UserStatus.PUBLIC;
        System.out.println("USERSTATE : "+userStatus + " " + this.getStatus());
    }
}
