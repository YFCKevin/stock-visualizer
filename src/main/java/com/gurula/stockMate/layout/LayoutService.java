package com.gurula.stockMate.layout;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LayoutService {
    List<LayoutSummaryDTO> getAllLayouts(String memberId);

    List<LayoutSummaryDTO> search(String memberId, String keyword);

    Result<Layout, String> save(StoredLayoutDTO layoutDTO);

    Result<Layout, String> edit(EditLayoutDTO layoutDTO);

    Result<String, String> delete(String id);

    Result<Layout, String> findById(String id);

    Result<Layout, String> constructNewLayout(String memberId, String symbolName, String interval);

    Result<Layout, String> findByIdAndMemberId(String layoutId, String memberId);

    Result<Layout, String> findLatestBySymbol(String symbolName, String memberId);

    Result<Layout, String> copyLayout(String layoutId, String memberId);
}
