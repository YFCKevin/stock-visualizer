package com.gurula.stockMate.member;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
}
