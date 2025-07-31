package com.gurula.stockMate.news;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody NewsDTO newsDTO) {
        final Member member = MemberContext.getMember();
        newsDTO.setMemberId(member.getId());

        try {
            Result<News, String> result = newsService.save(newsDTO);

            if (result.isOk()) {
                News news = result.unwrap();
                NewsDTO dto = news.toDto();
                return ResponseEntity.ok(dto);
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }


    }


    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestBody NewsDTO newsDTO) {
        final Member member = MemberContext.getMember();
        newsDTO.setMemberId(member.getId());

        try {
            Result<News, String> result = newsService.edit(newsDTO);

            if (result.isOk()) {
                News news = result.unwrap();
                NewsDTO dto = news.toDto();
                return ResponseEntity.ok(dto);
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @GetMapping("/date/{date}")
    public ResponseEntity<?> getAllNews(@PathVariable(name = "date") long date) {
        final Member member = MemberContext.getMember();

        try {
            Result<List<News>, String> result = newsService.getAllNewsByDate(date, member.getId());

            if (result.isOk()) {
                final List<News> news = result.unwrap();
                return ResponseEntity.ok(news.stream().map(News::toDto).toList());
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable(name = "id") String id) {
        final Member member = MemberContext.getMember();
        Map<String, Object> response = new HashMap<>();

        try {
            Result<String, String> result = newsService.deleteNews(id, member.getId());

            if (result.isOk()) {
                response.put("message", result.unwrap());
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                String errorMessage = result.unwrapErr();
                response.put("error", errorMessage);

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            response.put("error", errorMessage);

            if (errorMessage.equals("找不到對應的新聞資料")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    @GetMapping("/{newsId}")
    public ResponseEntity<?> getNewsById(@PathVariable String newsId) {
        final Member member = MemberContext.getMember();

        try {
            Result<NewsDTO, String> result = newsService.getNewsById(newsId, member.getId());
            if (result.isOk()) {
                return ResponseEntity.ok(result.unwrap());
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", errorMessage));
        }
    }
}
