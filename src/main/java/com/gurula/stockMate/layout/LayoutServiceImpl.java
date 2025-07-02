package com.gurula.stockMate.layout;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public List<LayoutSummaryDTO> search(String memberId, String name, String symbol, String symbolName) {
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

        if (StringUtils.isNotBlank(name)) {
            criteriaList.add(Criteria.where("name").regex(Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)));
        }

        if (StringUtils.isNotBlank(symbol)) {
            criteriaList.add(Criteria.where("symbolDoc.symbol").regex(Pattern.compile(Pattern.quote(symbol), Pattern.CASE_INSENSITIVE)));
        }

        if (StringUtils.isNotBlank(symbolName)) {
            criteriaList.add(Criteria.where("symbolDoc.name").regex(Pattern.compile(Pattern.quote(symbolName), Pattern.CASE_INSENSITIVE)));
        }

        if (!criteriaList.isEmpty()) {
            pipeline.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
        }

        Aggregation aggregation = Aggregation.newAggregation(pipeline);

        AggregationResults<Layout> results = mongoTemplate.aggregate(aggregation, "layout", Layout.class);

        return results.getMappedResults().stream()
                .map(LayoutSummaryDTO::construct)
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
}
