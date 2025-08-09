package com.gurula.stockMate.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/member")
@Tag(name = "Member API", description = "會員")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "取得會員資料")
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

    @Operation(summary = "取得所有會員資料")
    @GetMapping
    public ResponseEntity<?> getAllMembers() {
        Member member = MemberContext.getMember();
        List<MemberShortDTO> members = this.memberService.findAll();
        members = members.stream().filter(m -> !m.getId().equals(member.getId())).toList();
        return ResponseEntity.ok(members);
    }
}
