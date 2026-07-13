package dev.hyune.logstatistics.domain;

import dev.hyune.logstatistics.dto.LogStatisticsResult;

import java.time.LocalDateTime;

public class Job {
    private final String jobId;
    private JobStatus status;
    private LogStatisticsResult result;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Job(String jobId) {
        this.jobId = jobId;
        this.status = JobStatus.PROCESSING;
        this.createdAt = LocalDateTime.now();
    }

    public void complete(LogStatisticsResult result) {
        this.status = JobStatus.COMPLETED;
        this.result = result;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public String getJobId() {
        return jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public LogStatisticsResult getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
