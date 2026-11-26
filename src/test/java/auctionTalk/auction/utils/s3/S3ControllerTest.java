package auctionTalk.auction.utils.s3;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class S3ControllerTest {
    @Test
    void putEndpointKeepsStringResponseAndUsesAuthenticatedMember() throws Exception {
        S3Service service = mock(S3Service.class);
        var mvc = MockMvcBuilders.standaloneSetup(new S3Controller(service))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        var principal = new PrincipalDetails(Member.builder().id(7L).build(), Map.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        try {
            when(service.generatePresignedPutUrl("review", "photo.webp", 7L)).thenReturn("https://signed-put");
            mvc.perform(get("/s3/presigned/put").param("category", "review").param("fileName", "photo.webp"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("https://signed-put"));
            verify(service).generatePresignedPutUrl("review", "photo.webp", 7L);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getEndpointPreservesFileUrlParameterAndResponse() throws Exception {
        S3Service service = mock(S3Service.class);
        var mvc = MockMvcBuilders.standaloneSetup(new S3Controller(service)).build();
        when(service.generatePresignedGetUrl("review/old.webp")).thenReturn("https://signed-get");
        mvc.perform(get("/s3/presigned/get").param("fileUrl", "review/old.webp"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("https://signed-get"));
    }
}
