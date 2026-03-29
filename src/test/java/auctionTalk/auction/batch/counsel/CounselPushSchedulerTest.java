package auctionTalk.auction.batch.counsel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CounselPushSchedulerTest {

    @InjectMocks
    private CounselPushScheduler counselPushScheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job counselPushJob;

    @Test
    @DisplayName("스케줄러가 실행되면 상담 리뷰 알림 배치 작업을 실행")
    void runCounselPushJob_success() throws Exception {
        // when
        counselPushScheduler.runCounselPushJob();

        // then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        then(jobLauncher).should().run(eq(counselPushJob), captor.capture());

        JobParameters jobParameters = captor.getValue();
        assertThat(jobParameters).isNotNull();
        assertThat(jobParameters.getParameters()).containsKey("timestamp");
        assertThat(jobParameters.getLong("timestamp")).isNotNull();
    }
}