package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;

public interface ImportService {
    Result<String, String> importNewsToStudy(ImportDTO importDTO, String memberId);

    Result<String, String> importNotesToStudy(ImportDTO importDTO, String memberId);

    Result<String, String> importLayoutsToStudy(ImportDTO importDTO, String memberId);
}
