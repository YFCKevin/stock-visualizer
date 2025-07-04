package com.gurula.stockMate.layout;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface LayoutRepository extends MongoRepository<Layout, String> {
    List<Layout> findBySymbolId(String symbolId);

    Optional<Layout> findByIdAndMemberId(String layoutId, String memberId);
}
