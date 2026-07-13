package dev.hyune.logstatistics;

import dev.hyune.logstatistics.analyzer.LogAnalyzer;
import dev.hyune.logstatistics.dto.LogStatisticsResult;
import dev.hyune.logstatistics.parser.LogParser;
import dev.hyune.logstatistics.parser.LogRecord;
import dev.hyune.logstatistics.parser.kokoa.KokoaLogParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogStatisticsIntegrationTest {

    @Test
    void 실제로그파일_전체분석() throws Exception {
        // given
        LogParser parser = new KokoaLogParser();
        LogAnalyzer analyzer = new LogAnalyzer();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kokoa.log");
        assertNotNull(inputStream);

        // when
        List<LogRecord> records = parser.parseStream(inputStream);
        LogStatisticsResult result = analyzer.analyze(records);

        // then
        System.out.println("\n========== 실제 로그 파일 분석 결과 ==========");
        System.out.println("총 로그 수: " + records.size() + "건");
        System.out.println("\n1. 최다 호출 API Key: " + result.mostFrequentApiKey());
        System.out.println("\n2. 상위 3개 API Service:");
        result.topApiServices().forEach(s ->
            System.out.printf("   %s: %d건\n", s.apiServiceId(), s.requestCount())
        );
        System.out.println("\n3. 브라우저별 사용 비율:");
        result.browserUsageRatios().entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .forEach(e -> System.out.printf("   %s: %.2f%%\n", e.getKey(), e.getValue()));
        System.out.println("==========================================\n");

        assertTrue(records.size() > 0);
        assertNotNull(result.mostFrequentApiKey());
        assertEquals(3, result.topApiServices().size());
        assertFalse(result.browserUsageRatios().isEmpty());
    }
}
