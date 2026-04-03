package com.javatodev.finance.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkUri;

    @Value("${security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        ServerHttpSecurity httpSecurity = http
            .authorizeExchange(exchanges -> {

                //ALLOW USER REGISTRATION API ENDPOINT
                exchanges.pathMatchers("/user/api/v1/bank-users/register").permitAll();

                //ALLOW ACTUATOR ENDPOINTS
                exchanges.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/user/actuator/**").permitAll()
                    .pathMatchers("/fund-transfer/actuator/**").permitAll()
                    .pathMatchers("/banking-core/actuator/**").permitAll()
                    .pathMatchers("/utility-payment/actuator/**").permitAll();

                // Only require authentication if OAuth2 is enabled and JWK URI is configured
                if (oauth2Enabled && StringUtils.hasText(jwkUri)) {
                    exchanges.anyExchange().authenticated();
                } else {
                    exchanges.anyExchange().permitAll();
                }
            });

        httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable);

        // Only configure OAuth2 resource server if enabled and JWK URI is provided
        if (oauth2Enabled && StringUtils.hasText(jwkUri)) {
            httpSecurity.oauth2ResourceServer(oAuth2ResourceServer ->
                oAuth2ResourceServer.jwt(jwt -> jwt.jwkSetUri(jwkUri)));
        }

        return httpSecurity.build();

    }

}
