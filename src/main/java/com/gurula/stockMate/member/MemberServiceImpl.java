package com.gurula.stockMate.member;

import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsRepository;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;
    private final NoteRepository noteRepository;
    private final NewsRepository newsRepository;

    public MemberServiceImpl(MemberRepository memberRepository,
                             NoteRepository noteRepository,
                             NewsRepository newsRepository) {
        this.memberRepository = memberRepository;
        this.noteRepository = noteRepository;
        this.newsRepository = newsRepository;
    }

    @Override
    public Optional<Member> findByUserId(String userId) {
        return memberRepository.findByUserId(userId);
    }

    @Override
    public void save(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(String memberId) {
        return memberRepository.findById(memberId);
    }

    @Override
    public List<MemberShortDTO> findAll() {
        List<Member> members = this.memberRepository.findAll();
        return members.stream().map(member -> {
            MemberShortDTO dto = new MemberShortDTO();
            dto.setName(member.getName());
            dto.setId(member.getId());
            return dto;
        }).toList();
    }

    @Override
    public List<Note> getNotes(String memberId) {
        return noteRepository.findByMemberId(memberId);
    }

    @Override
    public List<News> getNews(String memberId) {
        return newsRepository.findByMemberId(memberId);
    }
}
