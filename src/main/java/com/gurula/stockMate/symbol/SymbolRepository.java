package com.gurula.stockMate.symbol;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SymbolRepository extends MongoRepository<Symbol, String> {
    Optional<Symbol> findBySymbol(String symbolId);
}
