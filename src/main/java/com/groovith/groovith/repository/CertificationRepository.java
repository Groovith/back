package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CertificationRepository extends CrudRepository<Certification, String> {
//    @Query("select c from Certification c where c.email=:email")
//    Optional<Certification> findByEmail(@Param(value = "email") String email);
}
