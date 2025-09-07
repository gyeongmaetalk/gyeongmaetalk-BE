package auctionTalk.auction.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI SpringCodeBaseAPI() {
        Info info = new Info()
                .title("경매톡 API")
                .description("경매톡 API 명세서")
                .version("1.0.0");

        final String ACCESS_SCHEME_NAME = "Access Token";
        final String REFRESH_SCHEME_NAME = "Refresh Token";

        Components components = new Components()
                // Authorization 헤더용 Access Token (bearer auth)
                .addSecuritySchemes(ACCESS_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                )
                // Refresh 헤더용 Refresh Token (apikey 방식)
                .addSecuritySchemes(REFRESH_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Refresh")
                );

        return new OpenAPI()
                .info(info)
                .components(components)
                .addServersItem(new Server().url("/"))
                // 원하는 경우 둘 다 글로벌로 적용 가능. 필요 시 각 API에 개별 지정 가능
                .addSecurityItem(new SecurityRequirement().addList(ACCESS_SCHEME_NAME))
                .addSecurityItem(new SecurityRequirement().addList(REFRESH_SCHEME_NAME));
    }
}
