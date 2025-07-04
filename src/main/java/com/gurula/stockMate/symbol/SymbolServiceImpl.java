package com.gurula.stockMate.symbol;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.symbol.dto.SymbolDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SymbolServiceImpl implements SymbolService{
    private final SymbolRepository symbolRepository;

    public SymbolServiceImpl(SymbolRepository symbolRepository) {
        this.symbolRepository = symbolRepository;
    }

    @Override
    public List<Symbol> getAllSymbols() {
        return symbolRepository.findAll();
    }

    @Override
    @Transactional
    public Result<String, String> delete(String id) {
        return symbolRepository.findById(id)
                .map(symbol -> {
                    symbolRepository.deleteById(id);
                    return Result.<String, String>ok("ok");
                })
                .orElseGet(() -> Result.err("找不到對應的 Symbol 資料"));
    }

    @Override
    @Transactional
    public Result<String, String> saveAll(List<SymbolDTO> symbolDTOList) {
        try {
            final List<Symbol> symbols = symbolDTOList.stream().map(SymbolDTO::toEntity).toList();
            symbolRepository.saveAll(symbols);
            return Result.ok("ok");
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Optional<Symbol> findById(String symbolId) {
        return symbolRepository.findById(symbolId);
    }
}
