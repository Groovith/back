package com.groovith.groovith.repository;

import com.groovith.groovith.domain.QFriend;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FriendRepositoryCustomImpl implements FriendRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
}
