package dev.hyune.logstatistics.parser.kokoa;

import dev.hyune.logstatistics.parser.LogParser;
import dev.hyune.logstatistics.parser.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KokoaLogParser implements LogParser {
    private static final Logger log = LoggerFactory.getLogger(KokoaLogParser.class);
    private static final Pattern LOG_PATTERN = Pattern.compile("\\[(\\d+)]\\[(.*?)]\\[(.*?)]\\[(.*?)]");

    @Override
    public List<LogRecord> parseStream(InputStream inputStream) throws IOException {
        log.info("Kokoa 로그 파싱 시작 (bracket 형식)");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<LogRecord> records = parseLines(reader);
            log.info("Kokoa 로그 파싱 완료: {} 줄 처리", records.size());
            return records;
        }
    }

    private List<LogRecord> parseLines(BufferedReader reader) throws IOException {
        List<LogRecord> records = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            try {
                LogRecord record = parseLine(line);
                records.add(record);
            } catch (IllegalArgumentException e) {
                log.warn("라인 파싱 실패: line={}, 이유={}", lineNumber, e.getMessage());
            }
        }

        return records;
    }

    private LogRecord parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid log format: " + line);
        }

        try {
            int statusCode = Integer.parseInt(matcher.group(1));
            String url = matcher.group(2);
            String browser = matcher.group(3);
            String timestampStr = matcher.group(4);
            LocalDateTime timestamp = LogRecord.parseTimestamp(timestampStr);

            return new LogRecord(statusCode, url, browser, timestamp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse log line: " + line, e);
        }
    }
}
