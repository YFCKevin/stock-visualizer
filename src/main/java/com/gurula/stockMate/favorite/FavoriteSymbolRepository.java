package com.gurula.stockMate.favorite;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteSymbolRepository extends MongoRepository<FavoriteSymbol, String> {
    Optional<FavoriteSymbol> findBySymbolIdAndMemberId(String symbolId, String memberId);

    Optional<FavoriteSymbol> findFirstByMemberIdOrderBySortOrderDesc(String memberId);

    List<FavoriteSymbol> findBySymbolIdInAndMemberId(List<String> symbolIds, String memberId);
}
