package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private User user;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @Builder
    public Image(String imageUrl){
        this.imageUrl = imageUrl;
    }
}
