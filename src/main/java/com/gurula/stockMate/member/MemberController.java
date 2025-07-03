package com.gurula.stockMate.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/member")
public class MemberController {


    @GetMapping("/info")
    public ResponseEntity<?> info() {
        final Member member = MemberContext.getMember();
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(member.getId());
        memberDTO.setUserId(member.getUserId());
        memberDTO.setPictureUrl(member.getPictureUrl());
        memberDTO.setCoverName(member.getCoverName());
        memberDTO.setName(member.getName());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setRole(member.getRole());
        memberDTO.setProvider(member.getProvider());
        memberDTO.setFavorites(member.getFavorites());
        memberDTO.setCreateAt(member.getCreateAt());
        memberDTO.setSuspendAt(member.getSuspendAt());
        return ResponseEntity.ok(Objects.requireNonNullElseGet(memberDTO, MemberDTO::new));
    }
}
