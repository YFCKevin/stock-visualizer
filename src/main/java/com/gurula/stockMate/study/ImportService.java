package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;

public interface ImportService {
    Result<StudyContentDTO, String> importNewsToStudy(ImportDTO importDTO, String memberId);

    Result<StudyContentDTO, String> importNotesToStudy(ImportDTO importDTO, String memberId);

    Result<StudyContentDTO, String> importLayoutsToStudy(ImportDTO importDTO, String memberId);
}
