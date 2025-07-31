package com.gurula.stockMate.news;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.newsAccessRule.NewsAccessRule;
import com.gurula.stockMate.newsAccessRule.NewsAccessRuleRepository;
import com.gurula.stockMate.newsAccessRule.VisibilityType;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcRepository;
import org.apache.commons.lang3.StringUtils;
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
            throw new RuntimeException("儲存失敗：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<List<News>, String> getAllNewsByDate(long date, String memberId) {
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
    }

    @Override
    @Transactional
    public Result<String, String> deleteNews(String id, String memberId) {
        try {
            Optional<News> opt = newsRepository.findByIdAndMemberId(id, memberId);
            if (opt.isEmpty()) {
                throw new RuntimeException("找不到對應的新聞資料");
            }

            newsRepository.deleteById(id);

            return Result.ok("刪除成功");

        } catch (Exception e) {
            throw new RuntimeException("刪除失敗：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<NewsDTO, String> getNewsById(String newsId, String memberId) {
        final Optional<News> opt = newsRepository.findByIdAndMemberId(newsId, memberId);
        if (opt.isEmpty()) {
            throw new RuntimeException("找不到對應的新聞資料");
        } else {
            final News news = opt.get();
            final String accessRuleId = news.getAccessRuleId();
            final Optional<NewsAccessRule> ruleOptional = newsAccessRuleRepository.findById(accessRuleId);
            if (ruleOptional.isEmpty()) {
                throw new RuntimeException("找不到對應的規則權限資料");
            } else {
                final NewsAccessRule newsAccessRule = ruleOptional.get();
                final NewsDTO dto = news.toDto();
                dto.setAccessRuleId(newsAccessRule.getId());
                dto.setVisibility(newsAccessRule.getVisibility());
                dto.setSelectedVisibleMembers(newsAccessRule.getVisibleToMemberIds().stream().toList());
                return Result.ok(dto);
            }
        }
    }

    @Override
    public Result<News, String> edit(NewsDTO newsDTO) {
        try {
            final String newsId = newsDTO.getId();
            final String memberId = newsDTO.getMemberId();
            final Optional<News> newsOptional = newsRepository.findByIdAndMemberId(newsId, memberId);
            if (newsOptional.isEmpty()) {
                throw new RuntimeException("找不到對應的新聞資料");
            } else {
                final News news = newsOptional.get();
                if (StringUtils.isNotBlank(newsDTO.getTitle()))
                    news.setTitle(newsDTO.getTitle());
                if (StringUtils.isNotBlank(newsDTO.getUrl()))
                    news.setUrl(newsDTO.getUrl());
                if (newsDTO.getPublishedAt() > 0)
                    news.setPublishedAt(newsDTO.getPublishedAt());
                if (StringUtils.isNotBlank(newsDTO.getContent()))
                    news.setContent(newsDTO.getContent());
                if (newsDTO.getTags() != null && !newsDTO.getTags().isEmpty())
                    news.setTags(newsDTO.getTags());
                if (StringUtils.isNotBlank(newsDTO.getAccessRuleId()))
                    news.setAccessRuleId(newsDTO.getAccessRuleId());

                final News saved = newsRepository.save(news);
                return Result.ok(saved);
            }
        } catch (Exception e) {
            throw new RuntimeException("儲存失敗：" + e.getMessage(), e);
        }
    }
}
