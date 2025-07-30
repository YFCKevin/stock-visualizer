package com.gurula.stockMate.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurula.stockMate.cache.CacheService;
import com.gurula.stockMate.config.ConfigProperties;
import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcRepository;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileWatcherService {
    private final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);
    private final Path folderPath;
    private WatchService watchService;
    private ExecutorService executor;
    private final OhlcRepository ohlcRepository;
    private final SymbolRepository symbolRepository;
    private final ConfigProperties configProperties;
    private final CacheService cacheService;
    private final Map<Path, LocalDate> processedDateMap = new HashMap<>();

    public FileWatcherService(OhlcRepository ohlcRepository, SymbolRepository symbolRepository, ConfigProperties configProperties, CacheService cacheService) {
        this.ohlcRepository = ohlcRepository;
        this.symbolRepository = symbolRepository;
        this.configProperties = configProperties;
        folderPath = Paths.get(configProperties.getOhlcDataStorePath());
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("正在啟動 WatchService...");
            if (!Files.exists(folderPath)) {
                logger.warn("資料夾不存在，將建立：" + folderPath);
                Files.createDirectories(folderPath);
            }

            watchService = FileSystems.getDefault().newWatchService();
            folderPath.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::processEvents);

            logger.info("WatchService 啟動成功，正在監聽：" + folderPath);
        } catch (Exception e) {
            logger.error("WatchService 啟動失敗：{}", e.getMessage(), e);
        }
    }

    private void processEvents() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take(); // 阻塞直到事件發生
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filename = (Path) event.context();
                    Path fullPath = folderPath.resolve(filename);

                    if (!filename.toString().endsWith(".json")) continue;

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        handleCreate(filename, fullPath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        handleModify(filename, fullPath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        logger.info("檔案刪除: {}", fullPath);
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Watch thread 被中斷，準備關閉...");
            } catch (Exception e) {
                logger.error("處理事件時發生非預期錯誤: {}", e.getMessage(), e);
            }
        }
    }

    private boolean shouldProcess(Path path, LocalDate fileDate) {
        LocalDate lastProcessedDate = processedDateMap.get(path);
        if (lastProcessedDate == null || lastProcessedDate.isBefore(fileDate)) {
            processedDateMap.put(path, fileDate);
            return true;
        }
        return false;
    }

    private void handleCreate(Path filename, Path fullPath) {
        try {
            List<Map<String, Object>> dataList = readJsonFile(fullPath);
            if (dataList.isEmpty()) return;

            String dateStr = (String) dataList.get(0).get("Date");
            LocalDate fileDate = LocalDate.parse(dateStr);

            if (!shouldProcess(fullPath, fileDate)) {
                logger.info("今天已處理過，忽略重複事件: {}", fullPath);
                return;
            }

            String symbol = extractSymbolFromFilename(filename);
            Optional<Symbol> symbolOpt = symbolRepository.findBySymbol(symbol);
            if (symbolOpt.isEmpty()) {
                logger.warn("找不到 Symbol 資料: {}", filename);
                return;
            }

            final Symbol symbolInfo = symbolOpt.get();

            if (dataList.isEmpty()) {
                logger.warn("新增檔案內容為空: {}", fullPath);
                return;
            }

            List<OhlcData> ohlcList = new ArrayList<>();
            for (Map<String, Object> item : dataList) {
                ohlcList.add(toOhlcData(item, symbolOpt.get().getId()));
            }

            ohlcRepository.saveAll(ohlcList);
            logger.info("成功儲存 {} 筆資料: {}", ohlcList.size(), filename);
            cacheService.evictCache(symbolInfo.getId(), IntervalType.ONE_DAY);
        } catch (Exception e) {
            logger.error("處理新增檔案 {} 發生錯誤: {}", filename, e.getMessage(), e);
        }
    }

    private void handleModify(Path filename, Path fullPath) {
        try {
            List<Map<String, Object>> dataList = readJsonFile(fullPath);
            if (dataList.isEmpty()) {
                logger.warn("修改檔案內容為空: {}", fullPath);
                return;
            }

            Map<String, Object> lastItem = dataList.get(dataList.size() - 1);

            String symbol = extractSymbolFromFilename(filename);
            Optional<Symbol> symbolOpt = symbolRepository.findBySymbol(symbol);
            if (symbolOpt.isEmpty()) {
                logger.warn("找不到 Symbol 資料: {}", filename);
                return;
            }

            final Symbol symbolInfo = symbolOpt.get();

            OhlcData ohlcData = toOhlcData(lastItem, symbolOpt.get().getId());
            ohlcRepository.save(ohlcData);
            logger.info("成功更新最新一筆資料: {}", filename);
            cacheService.evictCache(symbolInfo.getId(), IntervalType.ONE_DAY);
        } catch (Exception e) {
            logger.error("處理修改檔案 {} 發生錯誤: {}", filename, e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> readJsonFile(Path path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(path.toFile(), new TypeReference<>() {});
    }

    private OhlcData toOhlcData(Map<String, Object> item, String symbolId) {
        String date = (String) item.get("Date");

        OhlcData data = new OhlcData();
        data.setSymbolId(symbolId);
        data.setDate(date);
        data.setOpen(Double.parseDouble(item.get("Open").toString()));
        data.setHigh(Double.parseDouble(item.get("High").toString()));
        data.setLow(Double.parseDouble(item.get("Low").toString()));
        data.setClose(Double.parseDouble(item.get("Close").toString()));
        data.setVolume(Double.parseDouble(item.get("Volume").toString()));
        data.setInterval(IntervalType.ONE_DAY);
        data.setTimestamp(
                LocalDate.parse(date)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
        );
        return data;
    }

    private String extractSymbolFromFilename(Path filename) {
        return filename.getFileName().toString()
                .replaceFirst("(?i)\\.json$", "") // 去掉 .json（忽略大小寫）
                .replaceFirst("^\\^", "");        // 去掉開頭的 ^
    }

    @PreDestroy
    public void shutdown() {
        try {
            logger.info("正在關閉 WatchService 和執行緒池...");
            if (watchService != null) watchService.close();
            if (executor != null) executor.shutdownNow();
            logger.info("關閉完成。");
        } catch (IOException e) {
            logger.warn("關閉資源時發生錯誤: {}", e.getMessage(), e);
        }
    }
}
