package com.groovith.groovith.domain;

import com.groovith.groovith.domain.enums.UserRelationship;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.ChatRoomMemberDto;
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
    private String email;
    private String nickname;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
//    private List<Refresh> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE)
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.REMOVE)
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChatRoom> userChatRoom = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> friends = new ArrayList<>();

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> friendsAddedMe = new ArrayList<>();


    public ChatRoomMemberDto toUserChatRoomDto(UserRelationship userRelationship) {
        return new ChatRoomMemberDto(this, userRelationship);
    }

    public void updateStatus(UserStatus userStatus){
        this.status = (userStatus==UserStatus.PUBLIC) ? UserStatus.PRIVATE : UserStatus.PUBLIC;
    }

    public void updateImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public void addFriend(Friend friend){
        this.friends.add(friend);
    }
}
