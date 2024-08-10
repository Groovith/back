package com.groovith.groovith.repository;


import com.groovith.groovith.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
