package com.gurula.stockMate.layout;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.layout.dto.LayoutSummaryDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LayoutService {
    List<LayoutSummaryDTO> getAllLayouts(String memberId);

    List<LayoutSummaryDTO> search(String memberId, String name, String symbol, String symbolName);

    Result<Layout, String> save(LayoutDTO layoutDTO);

    Result<Layout, String> edit(LayoutDTO layoutDTO);

    Result<String, String> delete(String id);

    Result<Layout, String> findById(String id);

    Result<Layout, String> constructNewLayout(String memberId, String symbolName, String interval);
}
