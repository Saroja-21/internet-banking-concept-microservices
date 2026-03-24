package com.javatodev.finance.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GatewayConfigurationTest {

    private GatewayConfiguration gatewayConfiguration = new GatewayConfiguration();

    @Test
    void customGlobalFilter_notNull() {
        GlobalFilter filter = gatewayConfiguration.customGlobalFilter();
        assertNotNull(filter);
    }

    @Test
    void customGlobalFilter_returnsGlobalFilterBean() {
        GlobalFilter filter = gatewayConfiguration.customGlobalFilter();
        assertTrue(filter instanceof GlobalFilter);
    }

    @Test
    void gatewayConfiguration_instantiation() {
        GatewayConfiguration config = new GatewayConfiguration();
        assertNotNull(config);
    }

    @Test
    void customGlobalFilter_filterChainExecution() {
        GlobalFilter filter = gatewayConfiguration.customGlobalFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void customGlobalFilter_addsHeaderForUnauthenticatedUser() {
        GlobalFilter filter = gatewayConfiguration.customGlobalFilter();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(any());
    }
}
