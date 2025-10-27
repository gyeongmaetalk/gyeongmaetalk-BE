package auctionTalk.auction.batch.counsel;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CounselPushJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CounselPushTasklet counselPushTasklet;

    @Bean
    public Job counselPushJob() {
        return new JobBuilder("counselPushJob", jobRepository)
                .start(counselPushStep())
                .build();
    }

    @Bean
    public Step counselPushStep() {
        return new StepBuilder("counselPushStep", jobRepository)
                .tasklet(counselPushTasklet, transactionManager)
                .build();
    }
}