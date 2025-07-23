package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.LayoutSummaryDTO;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.note.NoteDTO;
import java.util.List;

public interface StudyService {
    Result<List<StudyContentDTO>, String> getContentsByStudyId(String studyId, String memberId);

    List<Study> findStudies(String memberId);

    Result<Study, String> saveTitleAndDesc(StudyDTO studyDTO, String memberId);

    Result<String, String> updateStudyContentItemsOrder(StudyContentReorderDTO dto, String memberId);

    Result<String, String> updateStudyNoteSyncState(ImportDTO importDTO, String memberId);

    Result<String, String> updateStudyLayoutSyncState(ImportDTO importDTO, String memberId);

    Result<String, String> updateStudyNews(ImportDTO importDTO, String memberId);

    List<LayoutSummaryDTO> searchLayoutContent(String memberId, String keyword);

    List<NoteDTO> searchNoteContent(String memberId, String keyword);

    List<News> searchNewsContent(String memberId, String keyword);

    Result<String, String> editContentItemTitle(UpdateStudyContentDTO updateStudyContentDTO);

    Result<String, String> removeContentItemFromStudy(RemoveContentItemDTO removeContentItemDTO);
}
