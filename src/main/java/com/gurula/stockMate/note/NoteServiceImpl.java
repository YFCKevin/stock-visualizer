package com.gurula.stockMate.note;

import com.gurula.stockMate.exception.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final MongoTemplate mongoTemplate;

    public NoteServiceImpl(NoteRepository noteRepository, MongoTemplate mongoTemplate) {
        this.noteRepository = noteRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Transactional
    public Result<Note, String> save(NoteDTO noteDTO) {
        try {
            final Note saved = noteRepository.save(noteDTO.toEntity());
            return Result.ok(saved);
        } catch (Exception e) {
            throw new RuntimeException("儲存失敗：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Note, String> edit(NoteDTO noteDTO) {
        try {
            final Optional<Note> opt = noteRepository.findById(noteDTO.getId());
            if (opt.isEmpty()) {
                throw new RuntimeException("找不到對應的 Note 資料");
            } else {
                final Note note = opt.get();

                if (StringUtils.isNotBlank(noteDTO.getTitle()))
                    note.setTitle(noteDTO.getTitle());
                if (noteDTO.getTags() != null && !noteDTO.getTags().isEmpty())
                    note.setTags(noteDTO.getTags());
                if (StringUtils.isNotBlank(noteDTO.getContent()))
                    note.setContent(noteDTO.getContent());
                note.setUpdatedAt(System.currentTimeMillis());

                final Note saved = noteRepository.save(note);

                return Result.ok(saved);
            }
        } catch (Exception e) {
            throw new RuntimeException("儲存失敗：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<List<Note>, String> findByLayoutId(String layoutId) {
        List<Note> notes = noteRepository.findByLayoutId(layoutId);
        if (notes.isEmpty()) {
            throw new RuntimeException("找不到對應的 Note 資料");
        } else {
            return Result.ok(notes);
        }
    }

    @Override
    public Result<Note, String> findByIdAndMemberId(String noteId, String memberId) {
        Optional<Note> opt = noteRepository.findByIdAndMemberId(noteId, memberId);
        if (opt.isEmpty()) {
            throw new RuntimeException("找不到對應的 Note 資料");
        } else {
            return Result.ok(opt.get());
        }
    }

    @Override
    @Transactional
    public Result<String, String> delete(String noteId, String memberId) {
        return noteRepository.findByIdAndMemberId(noteId, memberId)
                .map(note -> {
                    noteRepository.deleteById(noteId);
                    return Result.<String, String>ok("ok");
                })
                .orElseThrow(() -> new RuntimeException("找不到對應的 Note 資料"));
    }

    @Override
    public List<Note> search(String searchText) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("title").regex(searchText, "i"),
                Criteria.where("content").regex(searchText, "i")
        );

        Query query = new Query(criteria);

        return mongoTemplate.find(query, Note.class);
    }

    @Override
    @Transactional
    public Result<Note, String> editTitle(NoteDTO noteDTO) {
        try {
            final Optional<Note> opt = noteRepository.findByIdAndMemberId(noteDTO.getId(), noteDTO.getMemberId());
            if (opt.isEmpty()) {
                throw new RuntimeException("找不到對應的 Note 資料");
            } else {
                final Note note = opt.get();
                note.setTitle(noteDTO.getTitle());
                final Note saved = noteRepository.save(note);
                return Result.ok(saved);
            }
        } catch (Exception e) {
            throw new RuntimeException("找不到對應的 Note 資料");
        }
    }
}
