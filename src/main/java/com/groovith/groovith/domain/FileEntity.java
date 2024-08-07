package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FileEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "image_url")
    String imageUrl;

    @Builder
    public FileEntity(String imageUrl){
        this.imageUrl = imageUrl;
    }
}
