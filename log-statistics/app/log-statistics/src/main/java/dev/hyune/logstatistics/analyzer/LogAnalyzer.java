package dev.hyune.logstatistics.analyzer;

import dev.hyune.logstatistics.dto.ApiServiceCount;
import dev.hyune.logstatistics.dto.LogStatisticsResult;
import dev.hyune.logstatistics.parser.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class LogAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(LogAnalyzer.class);

    public LogStatisticsResult analyze(List<LogRecord> records) {
        log.info("통계 분석 시작: 총 {} 건", records.size());

        String mostFrequentApiKey = findMostFrequentApiKey(records);
        log.info("최다 호출 API Key: {}", mostFrequentApiKey);

        List<ApiServiceCount> topApiServices = findTopApiServices(records, 3);
        log.info("상위 API 서비스: {}", topApiServices.stream()
                .map(s -> s.apiServiceId() + "(" + s.requestCount() + "건)")
                .collect(Collectors.joining(", ")));

        Map<String, Double> browserUsageRatios = calculateBrowserUsageRatios(records);
        if (browserUsageRatios.isEmpty()) {
            log.info("브라우저 사용 비율: 데이터 없음");
        } else {
            log.info("브라우저 사용 비율: {}", browserUsageRatios.entrySet().stream()
                    .map(e -> e.getKey() + "(" + String.format("%.1f%%", e.getValue()) + ")")
                    .collect(Collectors.joining(", ")));
        }

        return new LogStatisticsResult(mostFrequentApiKey, topApiServices, browserUsageRatios);
    }

    /** 최다 호출 API Key (모든 상태코드 기준) */
    private String findMostFrequentApiKey(List<LogRecord> records) {
        Map<String, Long> apiKeyCount = records.stream()
                .map(LogRecord::extractApiKey)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        apiKey -> apiKey,
                        Collectors.counting()
                ));

        return apiKeyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** 상위 N개 API Service ID (모든 상태코드 기준) */
    private List<ApiServiceCount> findTopApiServices(List<LogRecord> records, int topN) {
        Map<String, Long> serviceCount = records.stream()
                .map(LogRecord::extractApiServiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        serviceId -> serviceId,
                        Collectors.counting()
                ));

        return serviceCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> new ApiServiceCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /** 브라우저별 사용 비율 (상태코드 200만, 백분율) */
    private Map<String, Double> calculateBrowserUsageRatios(List<LogRecord> records) {
        List<LogRecord> successRecords = records.stream()
                .filter(LogRecord::isSuccess)
                .filter(record -> record.browser() != null)  // browser가 null인 경우 제외 (maver 로그)
                .toList();

        if (successRecords.isEmpty()) {
            return Collections.emptyMap();
        }

        long totalCount = successRecords.size();

        Map<String, Long> browserCount = successRecords.stream()
                .collect(Collectors.groupingBy(
                        LogRecord::browser,
                        Collectors.counting()
                ));

        return browserCount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() * 100.0) / totalCount
                ));
    }
}
