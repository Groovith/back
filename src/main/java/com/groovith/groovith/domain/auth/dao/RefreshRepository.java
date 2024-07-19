package com.groovith.groovith.domain.auth.dao;

import com.groovith.groovith.domain.auth.domain.RefreshEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}