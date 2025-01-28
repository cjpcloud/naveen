package com.nationsbenefits.igloo.authengine.validator;

import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_ADJ_SUCCESS_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_ALLOW;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_AUTH_SUCCESS_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_INSUFFICIENT_FUND;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_INSUFFICIENT_FUND_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_INVALID_MERCHANT_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_MERCHANT_INVALID;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_PARTIAL_ALLOW_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_PARTIAL_ALLOW_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_PRODUCTS_DECLINED;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_PRODUCT_DECLINED_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_TRANSACTION_DECLINED;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_TRANSACTION_LIMIT_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_TRANSACTION_LIMIT_EXCEED;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_UNAUTHORIZED_LOCATION_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_UNAUTHORIZED_LOCATION_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_UNAUTHORIZED_STORE_LOCATION;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.DECLINED_AUTH_RESPONSE;
import static com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode.AUTH_DENY;
import static com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode.AUTH_EXCEEDED_TRANSACTION_LIMIT;
import static com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode.AUTH_INSUFFICIENT_FUNDS;
import static com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode.AUTH_INVALID_MERCHANT;
import static com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode.AUTH_PARTIAL_ALLOW;

/**
 * The {@code BASResponseValidator} class validates responses from the Basket Analysis Service (BAS)
 * during authorization and adjudication workflows.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Processes BAS status codes to determine if a transaction is authorized or declined.</li>
 *   <li>Generates and sends appropriate authorization or adjudication responses via {@code StreamObserver}.</li>
 *   <li>Logs validation outcomes for debugging and auditing.</li>
 * </ul>
 * <p>
 * Scenarios handled include invalid merchants, exceeded transaction limits, insufficient funds,
 * unauthorized locations, partial approvals, and declined transactions.
 * <p>
 * <strong>Thread-Safe:</strong> Stateless design ensures safe concurrent use.
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 * @author PwC
 */
@Slf4j
@Component
public class BASResponseValidator {


    /**
     * Validates the response from the Basket Analysis Service (BAS) and constructs an appropriate authorization response.
     *
     * @param analyzeBasketResponse The response object received from the BAS which contains information about the basket analysis.
     * @param request The original authorization request object.
     * @param responseObserver A stream observer to send the authorization response back to the client.
     * @return {@code true} if the BAS response indicates that the authorization is allowed, {@code false} otherwise.
     *
     * Validations performed:
     * <ul>
     *   <li>If {@code analyzeBasketResponse} is {@code null}, the method returns {@code true} indicating that no analysis was performed.</li>
     *   <li>Based on the {@code code} in {@code analyzeBasketResponse}, the following actions are taken:</li>
     *   <ul>
     *     <li><b>INVALID_MERCHANT:</b> Logs an "Invalid Merchant" message and constructs an authorization response with the code {@code AUTH_INVALID_MERCHANT}. The method returns {@code false}.</li>
     *     <li><b>EXCEEDED_TRANSACTION_LIMIT:</b> Logs a "Transaction Limit Exceeded" message and constructs an authorization response with the code {@code AUTH_EXCEEDED_TRANSACTION_LIMIT}. The method returns {@code false}.</li>
     *     <li><b>ALLOW:</b> Logs a "BAS authorization successful" message. The method returns {@code true}.</li>
     *     <li><b>INSUFFICIENT_FUNDS:</b> Logs an "Insufficient Funds" message and constructs an authorization response with the code {@code AUTH_INSUFFICIENT_FUNDS}. The method returns {@code false}.</li>
     *     <li><b>Default Case:</b> Logs a "BAS Validation Failed" message and constructs an authorization response with a generic decline code {@code AUTH_DENY}. The method returns {@code false}.</li>
     *   </ul>
     * </ul>
     */
    public boolean validateBASResponseForAuthorization(
        AnalyzeBasketResponse analyzeBasketResponse,
        AuthRequest request,
        StreamObserver<AuthResponse> responseObserver) {

        if (Objects.isNull(analyzeBasketResponse)) {
            return true; // Assuming validation passes if response is null
        }

        String statusCode = analyzeBasketResponse.getStatus().getCode();

        switch (statusCode) {
            case BAS_MERCHANT_INVALID:
                return handleBASResponseForAuth(BAS_INVALID_MERCHANT_DESC, request, AUTH_INVALID_MERCHANT, responseObserver, false);

            case BAS_TRANSACTION_LIMIT_EXCEED:
                return handleBASResponseForAuth(BAS_TRANSACTION_LIMIT_DESC, request, AUTH_EXCEEDED_TRANSACTION_LIMIT, responseObserver, false);

            case BAS_ALLOW:
                if(log.isInfoEnabled()) {
                    log.info(BAS_AUTH_SUCCESS_DESC);
                }
                return true;

            case BAS_PARTIAL_ALLOW_CODE:
                return handleBASResponseForAuth(BAS_PARTIAL_ALLOW_DESC,
                    request, AUTH_PARTIAL_ALLOW, responseObserver, true);

            case BAS_PRODUCTS_DECLINED:
                return handleBASResponseForAuth(BAS_PRODUCT_DECLINED_DESC,
                    request, AUTH_DENY, responseObserver, false);

            case BAS_INSUFFICIENT_FUND:
                return handleBASResponseForAuth(BAS_INSUFFICIENT_FUND_DESC, request, AUTH_INSUFFICIENT_FUNDS, responseObserver, false);

            case BAS_UNAUTHORIZED_STORE_LOCATION:
                return handleBASResponseForAuth(BAS_UNAUTHORIZED_LOCATION_DESC,
                    request, AUTH_INVALID_MERCHANT, responseObserver, false);

            default:
                return handleBASResponseForAuth(DECLINED_AUTH_RESPONSE, request, AUTH_DENY, responseObserver, false);
        }
    }

    /**
     * This method will handle the BAS response for authorization
     */
    private boolean handleBASResponseForAuth(String responseMessage, AuthRequest request, AuthResponseCode responseCode,
                                             StreamObserver<AuthResponse> responseObserver, boolean isValid) {

        if(log.isInfoEnabled()) {
            log.info(responseMessage);
        }
        AuthResponse response = AuthEngineServiceMapper.buildAuthResponse(responseMessage, request, responseCode);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return isValid;
    }

    /**
     * Validates the response from the Basket Analysis Service (BAS) and constructs an appropriate authorization response.
     *
     * @param analyzeBasketResponse The response object received from the BAS which contains information about the basket analysis.
     * @param responseObserver A stream observer to send the authorization response back to the client.
     * @return {@code true} if the BAS response indicates that the authorization is allowed, {@code false} otherwise.
     *
     * Validations performed:
     * <ul>
     *   <li>If {@code analyzeBasketResponse} is {@code null}, the method returns {@code true} indicating that no analysis was performed.</li>
     *   <li>Based on the {@code code} in {@code analyzeBasketResponse}, the following actions are taken:</li>
     *   <ul>
     *     <li><b>INVALID_MERCHANT:</b> Logs an "Invalid Merchant" message and constructs an authorization response with the code {@code AUTH_INVALID_MERCHANT}. The method returns {@code false}.</li>
     *     <li><b>EXCEEDED_TRANSACTION_LIMIT:</b> Logs a "Transaction Limit Exceeded" message and constructs an authorization response with the code {@code AUTH_EXCEEDED_TRANSACTION_LIMIT}. The method returns {@code false}.</li>
     *     <li><b>ALLOW:</b> Logs a "BAS authorization successful" message. The method returns {@code true}.</li>
     *     <li><b>INSUFFICIENT_FUNDS:</b> Logs an "Insufficient Funds" message and constructs an authorization response with the code {@code AUTH_INSUFFICIENT_FUNDS}. The method returns {@code false}.</li>
     *     <li><b>Default Case:</b> Logs a "BAS Validation Failed" message and constructs an authorization response with a generic decline code {@code AUTH_DENY}. The method returns {@code false}.</li>
     *   </ul>
     * </ul>
     */
    public boolean validateBASResponseForAdjudication(AnalyzeBasketResponse analyzeBasketResponse, StreamObserver<AdjudicationResponse> responseObserver) {
        if (Objects.isNull(analyzeBasketResponse)) {
            return true; // Assuming validation passes if analyzeBasketResponse is null
        }

        String statusCode = analyzeBasketResponse.getStatus().getCode();
        AdjudicationStatus adjudicationStatus;

        switch (statusCode) {
            case BAS_MERCHANT_INVALID ->
                adjudicationStatus = buildAdjudicationStatus(BAS_MERCHANT_INVALID, BAS_INVALID_MERCHANT_DESC);

            case BAS_TRANSACTION_LIMIT_EXCEED ->
                adjudicationStatus = buildAdjudicationStatus(BAS_TRANSACTION_LIMIT_EXCEED, BAS_TRANSACTION_LIMIT_DESC);

            case BAS_INSUFFICIENT_FUND -> adjudicationStatus = buildAdjudicationStatus(BAS_INSUFFICIENT_FUND, BAS_INSUFFICIENT_FUND_DESC);

            case BAS_PRODUCTS_DECLINED ->
                adjudicationStatus = buildAdjudicationStatus(BAS_PRODUCTS_DECLINED, BAS_PRODUCT_DECLINED_DESC);

            case BAS_PARTIAL_ALLOW_CODE -> adjudicationStatus = buildAdjudicationStatus(BAS_PARTIAL_ALLOW_CODE, BAS_PARTIAL_ALLOW_DESC);

            case BAS_UNAUTHORIZED_LOCATION_CODE -> adjudicationStatus = buildAdjudicationStatus(BAS_UNAUTHORIZED_LOCATION_CODE, BAS_UNAUTHORIZED_LOCATION_DESC);

            case BAS_ALLOW -> {
                log.info(BAS_ADJ_SUCCESS_DESC);
                return true;
            }
            default -> adjudicationStatus = buildAdjudicationStatus(DECLINED_AUTH_RESPONSE, BAS_TRANSACTION_DECLINED);
        }

        sendAdjudicationResponse(analyzeBasketResponse, adjudicationStatus, responseObserver);
        return false;
    }

    /**
     * Builds an AdjudicationStatus object.
     */
    private AdjudicationStatus buildAdjudicationStatus(String code, String description) {
        return AdjudicationStatus.newBuilder()
            .setCode(code)
            .setDesc(description)
            .build();
    }

    /**
     * Sends an adjudication response and logs the event.
     */
    private void sendAdjudicationResponse(AnalyzeBasketResponse analyzeBasketResponse, AdjudicationStatus adjudicationStatus, StreamObserver<AdjudicationResponse> responseObserver) {
        if(log.isInfoEnabled()) {
            log.info(adjudicationStatus.getDesc());
        }
        AdjudicationResponse response = AuthEngineServiceMapper.buildAdjudicationResponse(analyzeBasketResponse, adjudicationStatus);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }



}
