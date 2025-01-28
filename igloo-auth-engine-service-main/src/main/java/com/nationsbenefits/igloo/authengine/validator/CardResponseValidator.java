package com.nationsbenefits.igloo.authengine.validator;

import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_AUTHENTICATION_FAILED_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_AUTHENTICATION_FAILED_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_CVV_MISMATCH_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_CVV_MISMATCH_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_EXPIRED_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_EXPIRED_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_LOCKED_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_LOCKED_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_NOT_ACTIVATED_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_NOT_ACTIVATED_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_NUMBER_INVALID_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_NUMBER_INVALID_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_PIN_VALIDATION_FAILURE_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_PIN_VALIDATION_FAILURE_DESC;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_SUCCESS;

/**
 * The {@code CardResponseValidator} class validates card details responses received from a service
 * and handles various card status scenarios for both authorization and adjudication workflows.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validates card status and determines if a card is authorized or declined.</li>
 *   <li>Processes different card statuses such as invalid, expired, or authentication failed.</li>
 *   <li>Generates appropriate authorization or adjudication responses and sends them via {@code StreamObserver}.</li>
 *   <li>Logs validation outcomes for debugging and auditing.</li>
 * </ul>
 * <p>
 * Scenarios handled include invalid card numbers, inactive cards, locked cards, expired cards, CVV mismatches,
 * PIN validation failures, and other failure or success conditions.
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 * @author PwC
 */
@Slf4j
@Component
public class CardResponseValidator {

    /**
     * Validates the response of card details received from the service and handles various card status scenarios.
     *
     * @param cardDetailsResponse the response containing card details and its status.
     * @param request the authentication request containing details required for the validation process.
     * @param responseObserver the observer used to send the authentication response back to the client.
     * @return {@code true} if the card is valid and can be authorized; {@code false} otherwise.
     *
     * This method performs the following validations based on the card status:
     *  {@code CARD_NUMBER_INVALID}: If the card number (PAN hash) is invalid, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CARD_NOT_ACTIVATED}: If the card is found but not activated, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code SUCCESS}: If the card authorization is successful, it logs the event and returns {@code true}.
     *  {@code CARD_EXPIRED}: If the card is expired, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CARD_AUTHENTICATION_FAILED}: If the card type is not debit but the status is active, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CVV_MISMATCH}: If the card status is active but the CVV is invalid, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code PIN_VALIDATION_FAILURE}: If the card status is active but the PIN validation fails, it logs the event, sends a failure response, and returns {@code false}.
     *  Any other status: Logs the event, sends a generic card validation failure response, and returns {@code false}.
     *
     * <p>For each failure scenario, an {@code AuthResponse} object is built and sent to the client using the provided {@code responseObserver}.
     * Logging is performed for every scenario for audit and debugging purposes.
     *
     * <p><strong>Note:</strong> This method assumes that {@code cardDetailsResponse} is non-null.
     * If it is null, the method does not perform any action and returns {@code true}.
     */
    public boolean validateCardResponseForAuthorization(CardResponse cardDetailsResponse, AuthRequest request, StreamObserver<AuthResponse> responseObserver) {

        if (Objects.isNull(cardDetailsResponse)) {
            return true; // Assuming the validation passes if cardDetailsResponse is null
        }

        switch (cardDetailsResponse.getCardStatus()) {
            case CARD_NUMBER_INVALID:
                return handleCardResponseForAuth(CARD_NUMBER_INVALID_DESC, request, AuthResponseCode.AUTH_CARD_NUMBER_INVALID, responseObserver);

            case CARD_NOT_ACTIVATED:
                return handleCardResponseForAuth(CARD_NOT_ACTIVATED_DESC, request, AuthResponseCode.AUTH_CARD_NOT_ACTIVATED, responseObserver);

            case LOCKED:
                return handleCardResponseForAuth(CARD_LOCKED_DESC, request, AuthResponseCode.AUTH_CARD_LOCKED, responseObserver);

            case CARD_EXPIRED:
                return handleCardResponseForAuth(CARD_EXPIRED_DESC, request, AuthResponseCode.AUTH_CARD_EXPIRED, responseObserver);

            case CARD_AUTHENTICATION_FAILED:
                return handleCardResponseForAuth(CARD_AUTHENTICATION_FAILED_DESC, request, AuthResponseCode.AUTH_CARD_AUTHENTICATION_FAILED, responseObserver);

            case CVV_MISMATCH:
                return handleCardResponseForAuth(CARD_CVV_MISMATCH_DESC, request, AuthResponseCode.AUTH_CVV_MISMATCH, responseObserver);

            case PIN_VALIDATION_FAILURE:
                return handleCardResponseForAuth(CARD_PIN_VALIDATION_FAILURE_DESC, request, AuthResponseCode.AUTH_PIN_VALIDATION_FAILURE, responseObserver);

            default:
                if(log.isInfoEnabled()) {
                    log.info(CARD_SUCCESS);
                }
                return true;
        }
    }

    /**
     * This method will handle the Card response for authorization
     */
    private boolean handleCardResponseForAuth(String message, AuthRequest request, AuthResponseCode responseCode, StreamObserver<AuthResponse> responseObserver) {
        if(log.isInfoEnabled()) {
            log.info(message);
        }
        AuthResponse response = AuthEngineServiceMapper.buildAuthResponse(message, request, responseCode);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return false;
    }

    /**
     * Validates the response of card details received from the service and handles various card status scenarios.
     *
     * @param cardDetailsResponse the response containing card details and its status.
     * @param responseObserver the observer used to send the adjudication response back to the client.
     * @return {@code true} if the card is valid and can be authorized; {@code false} otherwise.
     *
     * This method performs the following validations based on the card status:
     *  {@code CARD_NUMBER_INVALID}: If the card number (PAN hash) is invalid, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CARD_NOT_ACTIVATED}: If the card is found but not activated, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code SUCCESS}: If the card authorization is successful, it logs the event and returns {@code true}.
     *  {@code CARD_EXPIRED}: If the card is expired, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CARD_AUTHENTICATION_FAILED}: If the card type is not debit but the status is active, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code CVV_MISMATCH}: If the card status is active but the CVV is invalid, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code PIN_VALIDATION_FAILURE}: If the card status is active but the PIN validation fails, it logs the event, sends a failure response, and returns {@code false}.
     *  {@code LOCKED}: Card found but status is locked, it logs the event, sends a failure response, and returns {@code false}.
     *  Any other status: Logs the event, sends a generic card validation failure response, and returns {@code false}.
     *
     * <p>For each failure scenario, an {@code AdjudicationResponse} object is built and sent to the client using the provided {@code responseObserver}.
     * Logging is performed for every scenario for audit and debugging purposes.
     *
     * This method assumes that {@code cardDetailsResponse} is non-null.
     * If it is null, the method does not perform any action and returns {@code true}.
     */
    public boolean validateCardResponseForAdjudication(CardResponse cardDetailsResponse, StreamObserver<AdjudicationResponse> responseObserver) {

        if (Objects.isNull(cardDetailsResponse)) {
            return true; // Assuming the validation passes if cardDetailsResponse is null
        }

        switch (cardDetailsResponse.getCardStatus()) {
            case CARD_NUMBER_INVALID:
                return handleCardResponseForAdjudication(CARD_NUMBER_INVALID_DESC, CARD_NUMBER_INVALID_CODE,responseObserver);

            case CARD_NOT_ACTIVATED:
                return handleCardResponseForAdjudication(CARD_NOT_ACTIVATED_DESC, CARD_NOT_ACTIVATED_CODE, responseObserver);

            case LOCKED:
                return handleCardResponseForAdjudication(CARD_LOCKED_DESC, CARD_LOCKED_CODE, responseObserver);

            case CARD_EXPIRED:
                return handleCardResponseForAdjudication(CARD_EXPIRED_DESC, CARD_EXPIRED_CODE,  responseObserver);

            case CARD_AUTHENTICATION_FAILED:
                return handleCardResponseForAdjudication(CARD_AUTHENTICATION_FAILED_DESC, CARD_AUTHENTICATION_FAILED_CODE, responseObserver);

            case CVV_MISMATCH:
                return handleCardResponseForAdjudication(CARD_CVV_MISMATCH_DESC, CARD_CVV_MISMATCH_CODE, responseObserver);

            case PIN_VALIDATION_FAILURE:
                return handleCardResponseForAdjudication(CARD_PIN_VALIDATION_FAILURE_DESC, CARD_PIN_VALIDATION_FAILURE_CODE, responseObserver);

            default:
                log.info(CARD_SUCCESS);
                return true;
        }
    }

    /**
     * This method will handle the Card response for adjudication
     */
    private boolean handleCardResponseForAdjudication(String message, String code,StreamObserver<AdjudicationResponse> responseObserver) {
        if(log.isInfoEnabled()) {
            log.info(message);
        }
        AdjudicationResponse response = AuthEngineServiceMapper.buildAdjudicationForCard(AdjudicationStatus.newBuilder().setCode(code).setDesc(message).build());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        return false;
    }

}
