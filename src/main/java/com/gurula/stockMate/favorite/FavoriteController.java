package com.gurula.stockMate.favorite;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.favorite.dto.CreateFavoriteSymbolDTO;
import com.gurula.stockMate.favorite.dto.FavoriteSymbolReorderDTO;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/favorite")
@Tag(name = "Favorite API", description = "收藏")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "添加股票到我的收藏")
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody CreateFavoriteSymbolDTO dto) {
        final Member member = MemberContext.getMember();
        Result<FavoriteSymbol, String> result = favoriteService.addFavoriteSymbol(dto.getSymbolId(), member.getId());
        return ResponseEntity.ok(result.unwrap());
    }

    @Operation(summary = "從我的收藏中移除股票")
    @DeleteMapping
    public ResponseEntity<?> remove(
            @Parameter(description = "從我的收藏中移除股票的symbol ID")
            @PathVariable String symbolId
    ) {
        final Member member = MemberContext.getMember();
        Result<String, String> result = favoriteService.removeFavoriteSymbol(symbolId, member.getId());
        return ResponseEntity.ok(result.unwrap());
    }

    @Operation(summary = "排序我的股票收藏")
    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@Valid @RequestBody FavoriteSymbolReorderDTO dto) {
        final Member member = MemberContext.getMember();
        dto.setMemberId(member.getId());
        Result<String, String> result = favoriteService.reorder(dto);
        return ResponseEntity.ok(result.unwrap());
    }
}
