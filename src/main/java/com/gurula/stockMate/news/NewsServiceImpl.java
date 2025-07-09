package com.gurula.stockMate.news;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.newsAccessRule.NewsAccessRule;
import com.gurula.stockMate.newsAccessRule.NewsAccessRuleRepository;
import com.gurula.stockMate.newsAccessRule.VisibilityType;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final NewsAccessRuleRepository newsAccessRuleRepository;
    private final OhlcRepository ohlcRepository;

    public NewsServiceImpl(NewsRepository newsRepository, NewsAccessRuleRepository newsAccessRuleRepository, OhlcRepository ohlcRepository) {
        this.newsRepository = newsRepository;
        this.newsAccessRuleRepository = newsAccessRuleRepository;
        this.ohlcRepository = ohlcRepository;
    }

    @Override
    @Transactional
    public Result<News, String> save(NewsDTO newsDTO) {
        try {
            final News saved = newsRepository.save(newsDTO.toEntity());
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<List<News>, String> getAllNewsByDate(long date, String memberId) {
        try {
            List<News> news = newsRepository.findByPublishedAt(date);

            final Set<String> accessRuleIds = news.stream()
                    .map(News::getAccessRuleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<String> matchedRuleIds = newsAccessRuleRepository.findByIdIn(accessRuleIds).stream()
                    .filter(newsAccessRule -> {
                        VisibilityType visibility = newsAccessRule.getVisibility();
                        if (visibility == VisibilityType.PUBLIC || visibility == VisibilityType.PRIVATE) {
                            return true;
                        }
                        if (visibility == VisibilityType.RESTRICTED) {
                            Set<String> visibleTo = newsAccessRule.getVisibleToMemberIds();
                            return visibleTo != null && visibleTo.contains(memberId);
                        }
                        return false;
                    })
                    .map(NewsAccessRule::getId)
                    .toList();

            List<News> visibleNews = news.stream()
                    .filter(n -> matchedRuleIds.contains(n.getAccessRuleId()))
                    .toList();

            return Result.ok(visibleNews);

        } catch (Exception e) {
            return Result.err("查詢失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<String, String> deleteNews(String id, String memberId) {
        final Optional<News> opt = newsRepository.findByIdAndMemberId(id, memberId);
        if (opt.isEmpty()) {
            return Result.err("找不到對應的新聞資料");
        } else {
            newsRepository.deleteById(id);
            return Result.ok("刪除成功");
        }
    }
}
