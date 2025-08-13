package com.gurula.stockMate.member;

import com.gurula.stockMate.news.News;
import com.gurula.stockMate.note.Note;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    Optional<Member> findByUserId(String userId);

    void save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(String memberId);

    List<MemberShortDTO> findAll();

    List<Note> getNotes(String memberId);

    List<News> getNews(String memberId);
}
