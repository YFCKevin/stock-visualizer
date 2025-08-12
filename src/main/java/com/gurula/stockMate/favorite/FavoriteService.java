package com.gurula.stockMate.favorite;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.favorite.dto.FavoriteSymbolReorderDTO;

public interface FavoriteService {
    Result<FavoriteSymbol, String> addFavoriteSymbol(String symbolId, String memberId);

    Result<String, String> removeFavoriteSymbol(String symbolId, String memberId);

    Result<String, String> reorder(FavoriteSymbolReorderDTO dto);
}
