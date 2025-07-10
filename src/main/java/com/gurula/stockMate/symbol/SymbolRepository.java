package com.gurula.stockMate.symbol;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SymbolRepository extends MongoRepository<Symbol, String> {
    Optional<Symbol> findBySymbol(String symbolName);

    List<Symbol> findByIdIn(Set<String> symbolSet);
}
