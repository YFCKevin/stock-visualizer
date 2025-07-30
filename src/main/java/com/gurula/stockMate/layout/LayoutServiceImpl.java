package com.gurula.stockMate.layout;

import cn.hutool.core.lang.Opt;
import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.layout.dto.LayoutSummaryDTO;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LayoutServiceImpl implements LayoutService{
    private final LayoutRepository layoutRepository;
    private final MongoTemplate mongoTemplate;
    private final SymbolRepository symbolRepository;

    public LayoutServiceImpl(LayoutRepository layoutRepository, MongoTemplate mongoTemplate,
                             SymbolRepository symbolRepository) {
        this.layoutRepository = layoutRepository;
        this.mongoTemplate = mongoTemplate;
        this.symbolRepository = symbolRepository;
    }

    @Override
    public List<LayoutSummaryDTO> getAllLayouts(String memberId) {
        Query query = new Query(Criteria.where("memberId").is(memberId));
        query.fields()
                .include("id")
                .include("name")
                .include("desc")
                .include("symbolId")
                .include("createAt")
                .include("updateAt");

        return mongoTemplate.find(query, LayoutSummaryDTO.class, "layout");
    }

    @Override
    public List<LayoutSummaryDTO> search(String memberId, String keyword) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        pipeline.add(Aggregation.addFields()
                .addField("symbolId")
                .withValue(ConvertOperators.ToObjectId.toObjectId("$symbolId"))
                .build());
        pipeline.add(Aggregation.lookup(
                "symbol",
                "symbolId",
                "_id",
                "symbolDoc"
        ));

        pipeline.add(Aggregation.unwind("symbolDoc", true));

        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.isNotBlank(memberId)) {
            criteriaList.add(Criteria.where("memberId").is(memberId));
        }

        if (StringUtils.isNotBlank(keyword)) {
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex(pattern),
                    Criteria.where("symbolDoc.symbol").regex(pattern),
                    Criteria.where("symbolDoc.name").regex(pattern)
            );
            criteriaList.add(keywordCriteria);
        }

        if (!criteriaList.isEmpty()) {
            pipeline.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
        }

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        AggregationResults<Layout> results = mongoTemplate.aggregate(aggregation, "layout", Layout.class);

        final List<Layout> layouts = results.getMappedResults();
        final Set<String> symbolSet = layouts.stream().map(Layout::getSymbolId).collect(Collectors.toSet());
        final Map<String, Symbol> symbolMap = symbolRepository.findByIdIn(symbolSet).stream().collect(Collectors.toMap(Symbol::getId, Function.identity()));

        return results.getMappedResults().stream()
                .map(layout -> {
                    LayoutSummaryDTO dto = new LayoutSummaryDTO();
                    dto = LayoutSummaryDTO.construct(layout);
                    if (symbolMap.containsKey(layout.getSymbolId())) {
                        final Symbol s = symbolMap.get(layout.getSymbolId());
                        dto.setSymbol(s.getSymbol());
                        dto.setSymbolName(s.getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Result<Layout, String> save(LayoutDTO layoutDTO) {
        try {
            final Symbol symbol = symbolRepository.findBySymbol(layoutDTO.getSymbol()).get();
            Layout saved = layoutRepository.save(layoutDTO.toEntity(symbol));
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }

    }

    @Override
    @Transactional
    public Result<Layout, String> edit(LayoutDTO layoutDTO) {
        try {
            Optional<Layout> optionalLayout = layoutRepository.findById(layoutDTO.getId());

            if (optionalLayout.isEmpty()) {
                return Result.err("找不到對應的 Layout 資料");
            }

            Layout layout = optionalLayout.get();

            if (StringUtils.isNotBlank(layoutDTO.getName()))
                layout.setName(layoutDTO.getName());
            if (StringUtils.isNotBlank(layoutDTO.getDesc()))
                layout.setDesc(layoutDTO.getDesc());
            if (StringUtils.isNotBlank(layoutDTO.getInterval()))
                layout.setInterval(IntervalType.fromValue(layoutDTO.getInterval()));

            if (layoutDTO.getUserSettings() != null && !layoutDTO.getUserSettings().isEmpty())
                layout.setUserSettings(layoutDTO.getUserSettings());

            layout.setUpdateAt(System.currentTimeMillis());

            Layout saved = layoutRepository.save(layout);
            return Result.ok(saved);

        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<String, String> delete(String id) {
        try {
            Optional<Layout> optionalLayout = layoutRepository.findById(id);
            if (optionalLayout.isEmpty()) {
                return Result.err("找不到對應的 Layout 資料");
            } else {
                layoutRepository.deleteById(id);
                return Result.ok("ok");
            }
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<Layout, String> findById(String id) {
        try {
            Optional<Layout> optionalLayout = layoutRepository.findById(id);
            if (optionalLayout.isEmpty()) {
                return Result.err("找不到對應的 Layout 資料");
            } else {
                return Result.ok(optionalLayout.get());
            }
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Layout, String> constructNewLayout(String memberId, String symbolName, String interval) {
        try {
            final Symbol symbol = symbolRepository.findBySymbol(symbolName).get();
            Layout layout = new Layout();
            layout.setMemberId(memberId);
            layout.setSymbolId(symbol.getId());
            layout.setInterval(IntervalType.fromValue(interval));
            layout.setCreateAt(System.currentTimeMillis());
            layout.setName("未命名");
            final Layout saved = layoutRepository.save(layout);
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<Layout, String> findByIdAndMemberId(String layoutId, String memberId) {
        Optional<Layout> opt = layoutRepository.findByIdAndMemberId(layoutId, memberId);
        if (opt.isEmpty()) {
            // 可能是 id 錯誤，或 memberId 對不上
            return Result.err("找不到對應的 Layout 資料");
        } else {
            return Result.ok(opt.get());
        }
    }

    @Override
    public Result<Layout, String> findLatestBySymbol(String symbolName, String memberId) {
        final Symbol symbol = symbolRepository.findBySymbol(symbolName).get();
        List<Layout> layouts = layoutRepository.findBySymbolIdAndMemberId(symbol.getId(), memberId);
        if (layouts.isEmpty()) {    // 建立新的 Layout
            return this.constructNewLayout(memberId, symbolName, "1d");
        } else {    // 取最新的 Layout
            Optional<Layout> latestLayout = layouts.stream()
                    .max(Comparator.comparingLong(layout ->
                            layout.getUpdateAt() > 0 ? layout.getUpdateAt() : layout.getCreateAt()
                    ));
            return Result.ok(latestLayout.get());
        }
    }

    @Override
    public Result<Layout, String> copyLayout(String layoutId, String memberId) {
        final Optional<Layout> opt = layoutRepository.findById(layoutId);
        if (opt.isEmpty()) {
            return Result.err("找不到對應的 Layout 資料");
        } else {
            final Layout layout = opt.get();
            layout.setId(null);
            layout.setMemberId(memberId);
            final Layout saved = layoutRepository.save(layout);
            return Result.ok(saved);
        }
    }
}
