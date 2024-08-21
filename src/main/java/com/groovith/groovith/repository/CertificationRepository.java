package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, String> {
    Optional<Certification> findByEmail(String email);
}
