package com.gurula.stockMate.favorite;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.favorite.dto.FavoriteSymbolDTO;
import com.gurula.stockMate.favorite.dto.FavoriteSymbolReorderDTO;
import com.gurula.stockMate.favorite.dto.ReorderItemDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService{
    private final FavoriteSymbolRepository favoriteSymbolRepository;

    public FavoriteServiceImpl(FavoriteSymbolRepository favoriteSymbolRepository) {
        this.favoriteSymbolRepository = favoriteSymbolRepository;
    }

    @Override
    public Result<FavoriteSymbol, String> addFavoriteSymbol(String symbolId, String memberId) {
        Optional<FavoriteSymbol> opt = favoriteSymbolRepository.findBySymbolIdAndMemberId(symbolId, memberId);
        if (opt.isPresent()) {
            throw new IllegalStateException("Symbol 已存在");
        } else {

            FavoriteSymbol favoriteSymbol = new FavoriteSymbol();
            favoriteSymbol.setSymbolId(symbolId);
            favoriteSymbol.setMemberId(memberId);
            favoriteSymbol.setCreatedAt(System.currentTimeMillis());

            Optional<FavoriteSymbol> favoriteSymbolOptional = favoriteSymbolRepository.findFirstByMemberIdOrderBySortOrderDesc(memberId);
            if (favoriteSymbolOptional.isPresent()) {
                final int currentSortOrder = favoriteSymbolOptional.get().getSortOrder();
                favoriteSymbol.setSortOrder(currentSortOrder + 1);
            } else {
                favoriteSymbol.setSortOrder(0);
            }

            return Result.ok(favoriteSymbolRepository.save(favoriteSymbol));
        }
    }

    @Override
    public Result<String, String> removeFavoriteSymbol(String symbolId, String memberId) {
        Optional<FavoriteSymbol> opt = favoriteSymbolRepository.findBySymbolIdAndMemberId(symbolId, memberId);
        if (opt.isEmpty()) {
            throw new RuntimeException("找不到對應的 FavoriteSymbol 資料");
        } else {
            favoriteSymbolRepository.delete(opt.get());
            return Result.ok("刪除成功");
        }
    }

    @Override
    @Transactional
    public Result<String, String> reorder(FavoriteSymbolReorderDTO dto) {

        Map<String, Integer> symbolIdToOrder = new HashMap<>();
        List<String> symbolIds = new ArrayList<>();
        for (ReorderItemDTO item : dto.getItems()) {
            symbolIdToOrder.put(item.getSymbolId(), item.getSortOrder());
            symbolIds.add(item.getSymbolId());
        }

        // 查詢資料
        List<FavoriteSymbol> favoriteSymbols =
                favoriteSymbolRepository.findBySymbolIdInAndMemberId(symbolIds, dto.getMemberId());

        if (favoriteSymbols.isEmpty()) {
            throw new RuntimeException(String.format(
                    "找不到對應的 FavoriteSymbol 資料, memberId=%s, symbolIds=%s",
                    dto.getMemberId(), symbolIds
            ));
        }

        // 更新順序
        favoriteSymbols.forEach(favoriteSymbol -> {
            Integer order = symbolIdToOrder.get(favoriteSymbol.getSymbolId());
            if (order != null) {
                favoriteSymbol.setSortOrder(order);
            }
        });

        try {
            favoriteSymbolRepository.saveAll(favoriteSymbols);
            return Result.ok("重新排序成功");
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }
}
