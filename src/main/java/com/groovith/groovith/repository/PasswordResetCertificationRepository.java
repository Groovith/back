package com.groovith.groovith.repository;

import com.groovith.groovith.domain.PasswordResetCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetCertificationRepository extends CrudRepository<PasswordResetCertification, String> {
}
