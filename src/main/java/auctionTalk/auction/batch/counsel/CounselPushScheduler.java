package auctionTalk.auction.batch.counsel;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounselPushScheduler {

    private final JobLauncher jobLauncher;
    private final Job counselPushJob;

    @Scheduled(cron = "0 10,40 * * * *")
    public void runCounselPushJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // Job 중복 방지
                .toJobParameters();

        jobLauncher.run(counselPushJob, jobParameters);
    }
}