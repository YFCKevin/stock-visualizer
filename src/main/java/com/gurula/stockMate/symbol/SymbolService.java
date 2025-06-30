package com.gurula.stockMate.symbol;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.symbol.dto.SymbolDTO;

import java.util.List;

public interface SymbolService {

    List<Symbol> getAllSymbols();

    Result<String, String> delete(String id);

    Result<String, String> saveAll(List<SymbolDTO> symbolDTOList);
}
