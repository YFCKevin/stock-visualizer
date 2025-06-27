package com.gurula.stockMate.member;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUserId(String userId);

    Optional<Member> findByEmail(String email);
}
