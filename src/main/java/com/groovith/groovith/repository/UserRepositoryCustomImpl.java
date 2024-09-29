package com.groovith.groovith.repository;

import com.groovith.groovith.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groovith.groovith.domain.QUser.user;

@RequiredArgsConstructor
@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<User> searchUser(String query, Pageable pageable, Long lastUserId) {
        List<User> users = jpaQueryFactory
                .selectFrom(user)
                .where(searchUserCondition(query, lastUserId))
                .limit(pageable.getPageSize())
                .fetch();
        return new SliceImpl<>(users);
    }

    private BooleanExpression searchUserCondition(String query, Long lastUserId) {
        // username, nickname 에 검색어 포함되는지
        BooleanExpression nameCondition = user.nickname.contains(query)
                .or(user.username.contains(query));
        // id 조건과 결합
        return nameCondition.and(isLastUserId(lastUserId));
    }

    // 첫번째 페이지일 경우(lastUserId == 0) 조건 무시
    private BooleanExpression isLastUserId(Long lastUserId) {
        if (lastUserId == null) {
            return null;
        }
        return user.id.gt(lastUserId);
    }

}
