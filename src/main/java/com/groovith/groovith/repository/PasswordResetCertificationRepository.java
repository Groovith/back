package com.groovith.groovith.repository;

import com.groovith.groovith.domain.PasswordResetCertification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetCertificationRepository extends JpaRepository<PasswordResetCertification, String> {
}
