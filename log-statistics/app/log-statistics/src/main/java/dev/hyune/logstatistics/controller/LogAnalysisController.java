package dev.hyune.logstatistics.controller;

import dev.hyune.logstatistics.domain.Job;
import dev.hyune.logstatistics.domain.JobStatus;
import dev.hyune.logstatistics.dto.LogStatisticsResult;
import dev.hyune.logstatistics.service.LogAnalysisJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/log-analysis")
public class LogAnalysisController {
    private static final Logger log = LoggerFactory.getLogger(LogAnalysisController.class);
    private final LogAnalysisJobService jobService;

    public LogAnalysisController(LogAnalysisJobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public JobCreatedResponse createAnalysisJob(@RequestParam MultipartFile file, @RequestParam String format) {
        log.info("분석 요청 수신: format={}, 파일명={}", format, file.getOriginalFilename());
        String jobId = jobService.createJob(file, format);
        log.info("Job 생성 완료: jobId={}", jobId);
        return new JobCreatedResponse(jobId);
    }

    @GetMapping("/jobs/{jobId}")
    public JobResponse getJob(@PathVariable String jobId) {
        log.debug("Job 조회 요청: jobId={}", jobId);
        Job job = jobService.getJob(jobId)
                .orElseThrow(() -> {
                    log.warn("Job 조회 실패 (존재하지 않음): jobId={}", jobId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
                });
        return JobResponse.from(job);
    }

    public record JobCreatedResponse(String jobId) {
    }

    public record JobResponse(
            String jobId,
            JobStatus status,
            LogStatisticsResult result,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        public static JobResponse from(Job job) {
            return new JobResponse(
                    job.getJobId(),
                    job.getStatus(),
                    job.getResult(),
                    job.getErrorMessage(),
                    job.getCreatedAt(),
                    job.getCompletedAt()
            );
        }
    }
}
