package dev.hyune.logstatistics.parser.maver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyune.logstatistics.parser.LogParser;
import dev.hyune.logstatistics.parser.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MaverLogParser implements LogParser {
    private static final Logger log = LoggerFactory.getLogger(MaverLogParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<LogRecord> parseStream(InputStream inputStream) throws IOException {
        log.info("Maver 로그 파싱 시작 (JSON 형식)");
        List<LogRecord> records = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    LogRecord record = parseLine(line);
                    records.add(record);
                } catch (Exception e) {
                    log.warn("라인 파싱 실패: line={}, 이유={}", lineNumber, e.getMessage());
                }
            }
        }

        log.info("Maver 로그 파싱 완료: {} 줄 처리", records.size());
        return records;
    }

    private LogRecord parseLine(String line) {
        try {
            JsonNode node = objectMapper.readTree(line);

            int statusCode = node.get("status_code").asInt();
            String url = node.get("url").asText();
            String serviceId = node.get("service_id").asText();
            String apiKey = node.get("api_key").asText();
            String timestampStr = node.get("@timestamp").asText();

            // maver는 browser 정보가 없으므로 null
            String browser = null;

            // ISO 8601 형식의 타임스탬프를 LocalDateTime으로 변환
            LocalDateTime timestamp = parseIsoTimestamp(timestampStr);

            // URL에 service_id와 apikey를 포함시켜서 extractApiServiceId(), extractApiKey()가 동작하도록 함
            // 형식: http://apis.maver.com/v1/{service_id}?apikey={api_key}
            String fullUrl = buildFullUrl(url, serviceId, apiKey);

            return new LogRecord(statusCode, fullUrl, browser, timestamp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON log line: " + line, e);
        }
    }

    private LocalDateTime parseIsoTimestamp(String timestampStr) {
        Instant instant = Instant.parse(timestampStr);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String buildFullUrl(String url, String serviceId, String apiKey) {
        // URL이 이미 service_id를 포함하고 있으므로, apikey 쿼리 파라미터만 추가
        if (apiKey != null && !apiKey.isEmpty()) {
            return url + "?apikey=" + apiKey;
        }
        return url;
    }
}
