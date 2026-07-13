package dev.hyune.logstatistics.dto;

import java.util.List;
import java.util.Map;

public record LogStatisticsResult(
        String mostFrequentApiKey,
        List<ApiServiceCount> topApiServices,
        Map<String, Double> browserUsageRatios
) {
}
