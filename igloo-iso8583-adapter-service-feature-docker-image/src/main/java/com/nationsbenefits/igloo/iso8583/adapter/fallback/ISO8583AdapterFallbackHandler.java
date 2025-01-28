package com.nationsbenefits.igloo.iso8583.adapter.fallback;

import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author PwC
 *
 * Fallback handler for ISO 8583 Service.
 * This class handles fallback processing in-case there is an exception in processing the downstream service response
 *
 */
@Slf4j
@Component
public class ISO8583AdapterFallbackHandler {


    /**
     * Fallback method for processing the Auth Engine response.
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param authorizationRequest the cAuthorization Request object containing the necessary data for requesting authorization of the transaction.
     * @return a default AuthResponse instance as a fallback.
     */
    public AuthResponse fallbackProcessAuthEngineResponse(AuthRequest authorizationRequest) {
        log.info("Inside Fallback processor for Auth Engine Response");
        /**
         * TODO: Implement stand-in processing for Auth Engine Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return AuthResponse.newBuilder().setStatusCode(AuthResponseCode.AUTH_ERROR.toString())
                .setIsoMessage(authorizationRequest.getIsoMessage())
                .setResponseCode(AuthResponseStatus.newBuilder().setAuthResponseCode(AuthResponseCode.AUTH_ERROR).setResponseDescription(AuthResponseCode.AUTH_ERROR.toString()).build()).build();

    }
}
