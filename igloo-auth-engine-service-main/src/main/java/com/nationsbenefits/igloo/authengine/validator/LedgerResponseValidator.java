package com.nationsbenefits.igloo.authengine.validator;

import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.LEDGER_ACCOUNT_NOT_FOUND;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.LEDGER_INSUFFICIENT_FUND;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.LEDGER_TRANSACTION_POSTED;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.LEDGER_VALIDATION_FAILED;

/**
 * The {@code LedgerResponseValidator} class validates responses from the Ledger Service
 * and constructs appropriate authorization responses based on the transaction status.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Processes ledger transaction statuses to determine if a transaction is successful or declined.</li>
 *   <li>Handles scenarios such as insufficient funds, account not found, and transaction posting errors.</li>
 *   <li>Generates and sends authorization responses via {@code StreamObserver}.</li>
 *   <li>Logs validation outcomes for debugging and auditing purposes.</li>
 * </ul>
 * <p>
 * Scenarios handled include successful transaction posting, insufficient funds, account issues,
 * and general validation failures.
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 * @author PwC
 */
@Slf4j
@Component
public class LedgerResponseValidator {

    /**
     * Validates the response from the Ledger Service and constructs an appropriate authorization response.
     *
     * @param postTransactionResponse The response object received from the Ledger Service which contains information about the transaction status.
     * @param request The original authorization request object.
     * @param responseObserver A stream observer to send the authorization response back to the client.
     * @return {@code true} if the Ledger response indicates that the transaction was posted successfully, {@code false} otherwise.
     *
     * Validations performed:
     * <ul>
     *   <li>If {@code postTransactionResponse} is {@code null}, the method returns {@code true} indicating that no validation was performed.</li>
     *   <li>Based on the {@code statusCode} in {@code postTransactionResponse.getTransactionStatus()}, the following actions are taken:</li>
     *   <ul>
     *     <li><b>INSUFFICIENT_FUND:</b> Logs an "Insufficient Funds" message and constructs an authorization response with the code {@code AUTH_INSUFFICIENT_FUNDS}. The method returns {@code false}.</li>
     *     <li><b>ACCOUNT_NOT_FOUND:</b> Logs an "Account Not Found" message and constructs an authorization response with the code {@code AUTH_ACCOUNT_NOT_FOUND}. The method returns {@code false}.</li>
     *     <li><b>TRANSACTION_POSTED:</b> Logs a "Ledger Posting Successful" message. The method returns {@code true}.</li>
     *     <li><b>Default Case:</b> Logs a "Ledger Validation Failed" message and constructs an authorization response with a generic error code {@code AUTH_TRANSACTION_ERROR}. The method returns {@code false}.</li>
     *   </ul>
     * </ul>
     */
    public Boolean validateLedgerResponseForAuthorization(PostTransactionResponse postTransactionResponse, AuthRequest request, StreamObserver<AuthResponse> responseObserver) {

        if (Objects.isNull(postTransactionResponse)) {
            return true; // Assuming validation passes if response is null
        }

        String statusMessage = postTransactionResponse.getTransactionStatus().getStatusMessage();
        switch (postTransactionResponse.getTransactionStatus().getStatusCode()) {
            case LEDGER_INSUFFICIENT_FUND:
                return handleLedgerResponseForAuth(
                    LEDGER_INSUFFICIENT_FUND, statusMessage,
                    request, AuthResponseCode.AUTH_INSUFFICIENT_FUNDS, responseObserver);

            case LEDGER_ACCOUNT_NOT_FOUND:
                return handleLedgerResponseForAuth(
                    LEDGER_ACCOUNT_NOT_FOUND, statusMessage,
                    request, AuthResponseCode.AUTH_ACCOUNT_NOT_FOUND, responseObserver);

            case LEDGER_TRANSACTION_POSTED:
                if(log.isInfoEnabled()) {
                    log.info(LEDGER_TRANSACTION_POSTED);
                }
                return true;

            default:
                return handleLedgerResponseForAuth(
                    LEDGER_VALIDATION_FAILED, statusMessage,
                    request, AuthResponseCode.AUTH_TRANSACTION_ERROR, responseObserver);
        }
    }

    /**
     * This method will handle the Ledger response for authorization
     */
    private Boolean handleLedgerResponseForAuth(String logMessage, String statusMessage, AuthRequest request, AuthResponseCode responseCode, StreamObserver<AuthResponse> responseObserver) {
        if(log.isInfoEnabled()) {
            log.info(logMessage);
        }
        AuthResponse response = AuthEngineServiceMapper.buildAuthResponse(statusMessage, request, responseCode);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return false;
    }

}
