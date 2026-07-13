package dev.hyune.logstatistics.domain;

public record LogAnalysisStartedEvent(Job job, byte[] fileBytes, String format) {
}
