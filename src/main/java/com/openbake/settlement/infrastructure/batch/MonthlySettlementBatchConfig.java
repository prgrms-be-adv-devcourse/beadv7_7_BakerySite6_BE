package com.openbake.settlement.infrastructure.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MonthlySettlementBatchConfig {

    public static final String JOB_NAME =
            "monthlySettlementJob";

    public static final String STEP_NAME =
            "monthlySettlementStep";

    private final MonthlySettlementTasklet monthlySettlementTasklet;

    @Bean
    public Job monthlySettlementJob(
            JobRepository jobRepository,
            Step monthlySettlementStep
    ) {
        return new JobBuilder(
                JOB_NAME,
                jobRepository
        )
                .start(monthlySettlementStep)
                .build();
    }

    @Bean
    public Step monthlySettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder(
                STEP_NAME,
                jobRepository
        )
                .tasklet(
                        monthlySettlementTasklet,
                        transactionManager
                )
                .build();
    }
}