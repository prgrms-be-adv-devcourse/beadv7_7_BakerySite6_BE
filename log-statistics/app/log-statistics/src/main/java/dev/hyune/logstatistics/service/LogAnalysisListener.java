package dev.hyune.logstatistics.service;

import dev.hyune.logstatistics.analyzer.LogAnalyzer;
import dev.hyune.logstatistics.domain.Job;
import dev.hyune.logstatistics.domain.LogAnalysisStartedEvent;
import dev.hyune.logstatistics.dto.LogStatisticsResult;
import dev.hyune.logstatistics.parser.LogParser;
import dev.hyune.logstatistics.parser.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class LogAnalysisListener {
    private static final Logger log = LoggerFactory.getLogger(LogAnalysisListener.class);
    private final Map<String, LogParser> parsers;
    private final LogAnalyzer logAnalyzer;

    public LogAnalysisListener(Map<String, LogParser> parsers, LogAnalyzer logAnalyzer) {
        this.parsers = parsers;
        this.logAnalyzer = logAnalyzer;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleLogAnalysisStarted(LogAnalysisStartedEvent event) {
        Job job = event.job();
        byte[] fileBytes = event.fileBytes();
        String format = event.format();

        // MDC에 jobId 설정 (모든 로그에 자동으로 포함됨)
        MDC.put("jobId", job.getJobId());

        try {
            log.info("분석 시작: format={}, 파일크기={} bytes", format, fileBytes.length);

            // format에 따라 적절한 parser 선택
            LogParser logParser = parsers.get(format);
            if (logParser == null) {
                throw new IllegalArgumentException("지원하지 않는 로그 포맷: " + format);
            }

            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                List<LogRecord> records = logParser.parseStream(inputStream);
                log.info("파싱 완료: {} 건", records.size());

                LogStatisticsResult result = logAnalyzer.analyze(records);
                job.complete(result);

                log.info("분석 완료");
            }
        } catch (Exception e) {
            log.error("분석 실패: {}", e.getMessage(), e);
            job.fail(e.getMessage());
        } finally {
            // MDC 정리 (메모리 누수 방지)
            MDC.remove("jobId");
        }
    }
}
