package com.gurula.stockMate.member;

import com.gurula.stockMate.member.dto.MemberDTO;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsDTO;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.dto.NoteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

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

    @Operation(summary = "取得會員的所有筆記")
    @GetMapping("/notes")
    public ResponseEntity<?> getAllNotes() {
        final Member member = MemberContext.getMember();
        List<Note> notes = memberService.getNotes(member.getId());
        final List<NoteDTO> noteDTOList = notes.stream().map(Note::toDto).toList();
        return ResponseEntity.ok(noteDTOList);
    }

    @Operation(summary = "取得會員的所有新聞")
    @GetMapping("/news")
    public ResponseEntity<?> getAllNews() {
        final Member member = MemberContext.getMember();
        List<News> news = memberService.getNews(member.getId());
        final List<NewsDTO> newsDTOList = news.stream().map(News::toDto).toList();
        return ResponseEntity.ok(newsDTOList);
    }
}
