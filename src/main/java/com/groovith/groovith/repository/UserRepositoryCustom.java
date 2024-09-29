package com.groovith.groovith.repository;

import com.groovith.groovith.domain.User;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;


import java.util.List;

// Querydsl 로 구현할 메서드 선언
@Repository
public interface UserRepositoryCustom {
    Slice<User> searchUser(String query, Pageable pageable, Long lastUserId);
}
