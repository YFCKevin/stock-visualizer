package com.gurula.stockMate.member;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUserId(String userId);

    Optional<Member> findByEmail(String email);

    List<Member> findByIdIn(Set<String> memberIds);
}
