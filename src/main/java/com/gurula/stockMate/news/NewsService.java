package com.gurula.stockMate.news;

import com.gurula.stockMate.exception.Result;

import java.util.List;

public interface NewsService {
    Result<News, String> save(NewsDTO newsDTO);

    Result<List<News>, String> getAllNewsByDate(long date, String memberId);

    Result<String, String> deleteNews(String id, String memberId);

    Result<NewsDTO, String> getNewsById(String newsId, String memberId);

    Result<News, String> edit(NewsDTO newsDTO);
}
