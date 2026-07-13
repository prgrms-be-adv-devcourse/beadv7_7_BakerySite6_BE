package dev.hyune.logstatistics.analyzer;

import dev.hyune.logstatistics.dto.LogStatisticsResult;
import dev.hyune.logstatistics.parser.LogRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogAnalyzerTest {

    @Test
    void analyze_복합시나리오() {
        // given
        LogAnalyzer analyzer = new LogAnalyzer();
        LocalDateTime now = LocalDateTime.now();
        List<LogRecord> records = List.of(
                new LogRecord(200, "http://apis.kokoa.com/search/blog?apikey=aaaa", "IE", now),
                new LogRecord(200, "http://apis.kokoa.com/search/blog?apikey=aaaa", "IE", now),
                new LogRecord(200, "http://apis.kokoa.com/search/blog?apikey=aaaa", "Firefox", now),
                new LogRecord(200, "http://apis.kokoa.com/search/news?apikey=bbbb", "Chrome", now),
                new LogRecord(404, "http://apis.kokoa.com/search/error", "Safari", now),
                new LogRecord(10, "http://apis.kokoa.com/search/vclip", "IE", now)
        );

        // when
        LogStatisticsResult result = analyzer.analyze(records);

        // then
        System.out.println("\n===== 분석 결과 =====");
        System.out.println("최다 호출 API Key: " + result.mostFrequentApiKey());
        System.out.println("\n상위 3개 API Service:");
        result.topApiServices().forEach(s ->
            System.out.println("  - " + s.apiServiceId() + ": " + s.requestCount() + "건")
        );
        System.out.println("\n브라우저별 사용 비율:");
        result.browserUsageRatios().forEach((browser, ratio) ->
            System.out.printf("  - %s: %.2f%%\n", browser, ratio)
        );
        System.out.println("===================\n");

        assertEquals("aaaa", result.mostFrequentApiKey());
        assertEquals(3, result.topApiServices().size());
        assertEquals("blog", result.topApiServices().get(0).apiServiceId());
        assertTrue(result.browserUsageRatios().get("IE") >= 50.0);
    }
}
