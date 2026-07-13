package dev.hyune.logstatistics.service;

import dev.hyune.logstatistics.domain.Job;
import dev.hyune.logstatistics.domain.LogAnalysisStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogAnalysisJobService {
    private static final Logger log = LoggerFactory.getLogger(LogAnalysisJobService.class);
    private final ConcurrentHashMap<String, Job> jobStore = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public LogAnalysisJobService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public String createJob(MultipartFile file, String format) {
        // 1. 파일을 먼저 읽음 (실패하면 Job 생성 안 함)
        byte[] fileBytes;
        try (InputStream inputStream = file.getInputStream()) {
            fileBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        // 2. 파일 읽기 성공 후 Job 생성
        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId);
        jobStore.put(jobId, job);

        log.info("Job 생성: jobId={}, format={}, 파일명={}", jobId, format, file.getOriginalFilename());

        // 3. 이벤트 발행하여 비동기 분석 시작
        eventPublisher.publishEvent(new LogAnalysisStartedEvent(job, fileBytes, format));

        return jobId;
    }

    public Optional<Job> getJob(String jobId) {
        return Optional.ofNullable(jobStore.get(jobId));
    }
}
