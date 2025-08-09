package com.gurula.stockMate.symbol;

import com.gurula.stockMate.exception.Result;

import java.util.List;
import java.util.Optional;

public interface SymbolService {

    List<SymbolDataDTO> getAllSymbols();

    Result<String, String> delete(String id);

    Result<String, String> saveAll(List<SymbolDTO> symbolDTOList);

    Optional<Symbol> findById(String symbolId);

    List<Symbol> search(String name);
}
