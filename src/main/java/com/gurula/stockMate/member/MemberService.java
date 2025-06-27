package com.gurula.stockMate.member;

import java.util.Optional;

public interface MemberService {
    Optional<Member> findByUserId(String userId);

    void save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(String memberId);
}
