package com.gurula.stockMate.news;

import cn.hutool.core.lang.Opt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends MongoRepository<News, String> {
    List<News> findByIdInAndMemberId(List<String> ids, String memberId);

    Optional<News> findByIdAndMemberId(String newsId, String memberId);

    List<News> findByPublishedAt(long date);
}
