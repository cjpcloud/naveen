package com.nationsbenefits.igloo.iso8583.adapter.client;

import com.nationsbenefits.igloo.authengine.grpc.*;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import com.nationsbenefits.igloo.iso8583.adapter.constant.EventConstant;
import com.nationsbenefits.igloo.iso8583.adapter.exception.ISOAdapterCircuitBreakerFallbackException;
import com.nationsbenefits.igloo.iso8583.adapter.exception.ISOAdapterExceptionHandler;
import com.nationsbenefits.igloo.iso8583.adapter.fallback.ISO8583AdapterFallbackHandler;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.nationsbenefits.igloo.iso8583.adapter.constant.ISOAdapterConstant.MESSAGE_TYPE_AUTHORIZATION;
import static com.nationsbenefits.igloo.iso8583.adapter.util.EventPublisherUtil.buildEventHeader;
import static com.nationsbenefits.igloo.iso8583.adapter.util.EventPublisherUtil.buildEventPayload;

/**
 *
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * This is delegator class to connect external services through gRPC call
 */
@Component
@Slf4j
public class ISO8583AdapterClientDelegate {

    private final ManagedChannel managedChannel;

    private final com.nationsbenefits.igloo.authengine.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub transactionServiceStub;

    private final Retry retry;

    @Autowired
    public ISO8583AdapterClientDelegate(ManagedChannel managedChannel,Retry retry) {
        this.managedChannel = managedChannel;
        this.retry = retry;
        transactionServiceStub =
                AuthorizationServiceGrpc.newBlockingStub(this.managedChannel);
    }


    /**
     * The port of the Authorization Engine Service.
     */
    @Value("${authengine.service.port}")
    private String authenginePort;

    /**
     * The host address of the Authorization Engine Service.
     */
    @Value("${authengine.service.host}")
    private String authengineHost;

    @Value("${authengine.service.deadlineTimeout}")
    private int deadlineTimeout;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.version}")
    private String version;

    /**
     * The circuitBreaker object
     */
    private CircuitBreaker circuitBreaker;

    /**
     * The CircuitBreakerRegistry Object
     */
    @Autowired
    private CircuitBreakerRegistry registry;

    /**
     *  The ISO8583AdapterFallbackHandler object
     */
    @Autowired
    private ISO8583AdapterFallbackHandler iso8583AdapterFallbackHandler;

    @Autowired
    private ISOAdapterExceptionHandler isoAdapterExceptionHandler;

    @Autowired
    private EventPublisherService eventPublisherService;

    /**
     * Processes an authorization transaction by sending a gRPC request to the Auth Engine.
     * The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     * @param authorizationRequest the authorization request object containing the necessary data for authorization.
     * @return the authorization response object received from the Auth Engine.
     * @throws ISOAdapterCircuitBreakerFallbackException if the authorization request fails after all retries.
     */

    public AuthResponse processAuthTransaction(AuthRequest authorizationRequest) {
        log.info("gRPC Request Sent to Auth Engine accepting canonical data model for authorization") ;
        this.circuitBreaker = registry.circuitBreaker("ISO8583Adapter");


        Decorators.DecorateSupplier<AuthResponse> decoratedSupplier = Decorators.ofSupplier(() -> {
                    AuthResponse authResponse = null;
                    try {
                        authResponse = transactionServiceStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).processAuthorizationRequest(authorizationRequest);
                    } catch (StatusRuntimeException e) {
                        eventPublisherService.publishEventAsync(buildEventHeader(authorizationRequest,MESSAGE_TYPE_AUTHORIZATION, appName, version), List.of(buildEventPayload(EventConstant.AUTHORIZATION_FAILED,e.getMessage())));

                        log.error("Exception thrown from Auth Engine server : {} : {}",
                                e.getStatus().getCode(), e.getStatus().getDescription());
                        isoAdapterExceptionHandler.handleRuntimeException(e);
                        throw new ISOAdapterCircuitBreakerFallbackException("Exception processing Authorization Engine Response");
                    }
                    return authResponse;
                }).withCircuitBreaker(circuitBreaker).withRetry(retry)
                .withFallback(throwable -> iso8583AdapterFallbackHandler.fallbackProcessAuthEngineResponse(authorizationRequest));

        return decoratedSupplier.get();
    }


}
