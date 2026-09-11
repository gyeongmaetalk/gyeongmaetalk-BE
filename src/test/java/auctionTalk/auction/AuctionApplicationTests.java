package auctionTalk.auction;

import auctionTalk.auction.config.FireBaseConfig;
import auctionTalk.auction.config.security.auth.AppleClientSecretProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootTest
class AuctionApplicationTests {

	@MockBean
	private AppleClientSecretProvider appleClientSecretProvider;

	@MockBean
	private FireBaseConfig fireBaseConfig;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}
}