/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine.service;

import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.authengine.client.AuthEngineClientDelegate;
import com.nationsbenefits.igloo.authengine.constant.EventConstant;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.grpc.AuthorizationServiceGrpc;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.authengine.validator.BASResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.CardResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.LedgerResponseValidator;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.domain.event.EventPayload;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionResponse;
import com.nationsbenefits.igloo.member.models.protobuf.MemberResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_CARD_DETAILS_COMPLETED;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_ENTRY_LOG;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_EXECUTION_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_INTERRUPT_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.CARD_SERVICE_INVOKE_LOG;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.addTimeStampToHeader;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.buildEventHeaderFromAuthorizationModel;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.buildEventPayload;

/**
 * gRPC service class for performing authorization for both MasterCard and NYCE payment networks.
 * This class validates cards and accounts by invoking various gRPC services asynchronously.
 *
 * <p>The class uses the scatter-gather design pattern to make multiple asynchronous service calls
 * and gather their results to process the authorization request.</p>
 *
 * <p>This class is annotated with {@link GrpcService} to indicate that it is a gRPC service,
 * and with {@link Slf4j} for logging purposes.</p>
 *
 * @author PwC
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthorizationService extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {

    private final AuthEngineClientDelegate authEngineClientDelegate;
    private final CardResponseValidator cardResponseValidator;
    private final BASResponseValidator basResponseValidator;
    private final LedgerResponseValidator ledgerResponseValidator;
    private final EventPublisherService eventPublisherService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.version}")
    private String version;


    /**
     * Processes the authorization request by validating the card, account, BAS, and fraud transaction services.
     *
     * <p>This method uses the scatter-gather design pattern to make multiple asynchronous service calls
     * and gather their results to process the authorization request.</p>
     *
     * <p>The method performs the following steps:
     * <ol>
     *     <li>Invokes the Card Service to fetch card details.</li>
     *     <li>Invokes the Transaction Fraud Service to validate the transaction for fraud.</li>
     *     <li>Checks the card service response to retrieve the member ID.</li>
     *     <li>Invokes the Member Service to fetch member details.</li>
     *     <li>Invokes the Member Account Service to fetch account details.</li>
     *     <li>Combines the results of the above service calls.</li>
     *     <li>Invokes the BAS service to analyze the basket.</li>
     *     <li>Invokes the Ledger Service to post the transaction.</li>
     * </ol>
     * </p>
     *
     * @param request          the AuthRequest containing the PAN number of the card and other details
     * @param responseObserver the StreamObserver to send the response back to the client
     */
    @Override
    public void processAuthorizationRequest(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
        if(log.isInfoEnabled()) {
            log.info(AUTH_SERVICE_ENTRY_LOG, request.getIsoMessage().getTransaction().getID());
        }

        EventHeader eventHeader = buildEventHeaderFromAuthorizationModel(request, appName, version);
        publishEvent(eventHeader, EventConstant.ADJUDICATION_MESSAGE_RECEIVED, request.toString());

        try {
            // Scatter: Call Card Service and Fraud Service. Do not gather results at this point

            // 1. Invoke Card Service
            log.debug(CARD_SERVICE_INVOKE_LOG);
            CompletableFuture<CardResponse> cardResponse = authEngineClientDelegate.processCardServiceResponse(AuthEngineServiceMapper.buildCardQuery(request));

            // 2. Invoke Transaction Fraud Service
            CompletableFuture<AnalyzeTransactionResponse> fraudResponse = authEngineClientDelegate.invokeTransactionFraudService(AuthEngineServiceMapper.buildAnalyzeFraudTxnReq(request));

            // Gather: Combine the results from Card Service, Fraud Service
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(fraudResponse, cardResponse);
            combinedFuture.get();
            //validating the card response
            if (!cardResponseValidator.validateCardResponseForAuthorization(cardResponse.get(), request, responseObserver)) {
                return;
            }

            log.debug(AUTH_SERVICE_CARD_DETAILS_COMPLETED);

            CompletableFuture<MemberResponse> memberResponse = null;
            CompletableFuture<AccountDetailsResponse> accountResponse = null;

            // 3. Check the Card Service response to retrieve the member ID
            if (Objects.nonNull(cardResponse.get())) {
                if(log.isInfoEnabled()) {
                    log.info(AUTH_SERVICE_CARD_DETAILS_COMPLETED);
                }
                publishEvent(eventHeader, EventConstant.ADJUDICATION_CARD_DETAILS_VALIDATED, cardResponse.get().toString());

                // Retrieve the Member Account ID
                String memberAccountId = cardResponse.get().getCard().getMemberAccountId();

                // 4. Invoke the Member Account Service
                accountResponse = authEngineClientDelegate.invokeMemberAccountService(AuthEngineServiceMapper.buildAccountQuery(memberAccountId));

                //Retrieve Member ID
                String memberId = accountResponse.get().getMemberAccount().getMemberID();
                // 5. Invoke the Member Service
                memberResponse = authEngineClientDelegate.invokeMemberService(AuthEngineServiceMapper.buildMemberQuery(memberId));


            } else {
                publishEvent(eventHeader, EventConstant.ADJUDICATION_CARD_DETAILS_NOT_FOUND, cardResponse.get().toString());
            }

            publishEvent(eventHeader, EventConstant.ADJUDICATION_MEMBER_DETAILS_VALIDATED, memberResponse.get().toString());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_MEMBER_ACCOUNT_DETAILS_VALIDATED, accountResponse.get().toString());

            // 7. Invoke BAS Service
            AnalyzeBasketResponse analyzeBasketResponse = authEngineClientDelegate.invokeBAS(AuthEngineServiceMapper.buildBASRequest(request, memberResponse.get(), accountResponse.get()));

            if (!basResponseValidator.validateBASResponseForAuthorization(analyzeBasketResponse, request, responseObserver)) {
                publishEvent(eventHeader, EventConstant.ADJUDICATION_BAS_ADJUDICATION_FAILED, analyzeBasketResponse.toString());
                return;
            }
            publishEvent(eventHeader, EventConstant.ADJUDICATION_BAS_ADJUDICATION_SUCCESS, analyzeBasketResponse.toString());

            // 8. Invoke Ledger Service
            PostTransactionResponse postTransactionResponse = authEngineClientDelegate.invokePostLedgerTransactionService(AuthEngineServiceMapper.buildPostTransactionRequest(accountResponse.get(), analyzeBasketResponse));
            if (!ledgerResponseValidator.validateLedgerResponseForAuthorization(postTransactionResponse, request, responseObserver)) {
                publishEvent(eventHeader, EventConstant.ADJUDICATION_LEDGER_POST_FAILED, postTransactionResponse.toString());
                return;
            }
            publishEvent(eventHeader, EventConstant.ADJUDICATION_LEDGER_POST_SUCCESS, postTransactionResponse.toString());

            AuthResponse response = AuthEngineServiceMapper.buildAuthResponse(AuthResponseCode.AUTH_ALLOW.toString(), request, AuthResponseCode.AUTH_ALLOW);
            //Publishing ADJUDICATION_SUCCESS_RESPONSE_CREATED event
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SUCCESS_RESPONSE_CREATED, response.toString());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            publishEvent(eventHeader, EventConstant.ADJUDICATION_SUCCESS_RESPONSE_SENT, response.toString());
        } catch (InterruptedException ex) {
            log.error(AUTH_SERVICE_INTERRUPT_EXCEPTION, ex.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_INTERRUPT_EXCEPTION_OCCURRED, ex.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            log.error(AUTH_SERVICE_EXECUTION_EXCEPTION, ex.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SERVICE_EXECUTION_EXCEPTION_OCCURRED, ex.getMessage());
            responseObserver.onError(ex);
        } catch (Exception e) {
            log.error(AUTH_SERVICE_EXCEPTION, e.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SERVICE_EXECUTION_EXCEPTION_OCCURRED, e.getMessage());
            responseObserver.onError(e);
        }
    }


    /**
     * Publishes an event with the specified header, name, and payload.
     * <p>
     * This method updates the event header with a timestamp, constructs an event payload
     * using the provided event name and payload data, and publishes the event asynchronously.
     * <p>
     * Key steps performed:
     * <ul>
     *   <li>Updates the event header by adding a timestamp.</li>
     *   <li>Builds an event payload using the provided event name and payload data.</li>
     *   <li>Publishes the event asynchronously using {@code eventPublisherService}.</li>
     * </ul>
     *
     * @param eventHeader The original event header to be updated with a timestamp.
     * @param eventName   The name of the event to be published.
     * @param payload     The payload data associated with the event.
     */
    private void publishEvent(EventHeader eventHeader, String eventName, String payload) {
        if(log.isInfoEnabled()) {
            log.info("Publishing the event {} :", eventName);
        }
        EventHeader updatedHeader = addTimeStampToHeader(eventHeader);
        EventPayload eventPayload = buildEventPayload(eventName, payload);
        eventPublisherService.publishEventAsync(updatedHeader, List.of(eventPayload));
    }

}




