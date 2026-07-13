package dev.hyune.logstatistics.dto;

public record ApiServiceCount(
        String apiServiceId,
        long requestCount
) {
}
