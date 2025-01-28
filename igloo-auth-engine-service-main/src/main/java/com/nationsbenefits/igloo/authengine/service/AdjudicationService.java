/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine.service;

import com.nationsbenefits.igloo.authengine.client.AuthEngineClientDelegate;
import com.nationsbenefits.igloo.authengine.constant.EventConstant;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationRequest;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.authengine.grpc.AplAdjudicationServiceGrpc;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.authengine.validator.BASResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.CardResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.FraudResponseValidator;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.domain.event.EventPayload;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_SUCCESS;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_SUCCESS_DESC;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.ADJ_SERVICE_ENTRY_LOG;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_CARD_DETAILS_COMPLETED;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_EXECUTION_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.AUTH_SERVICE_INTERRUPT_EXCEPTION;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.CARD_SERVICE_INVOKE_LOG;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.addTimeStampToHeader;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.buildEventHeaderFromAdjudicationModel;
import static com.nationsbenefits.igloo.authengine.util.EventBuilderUtil.buildEventPayload;

/**
 * gRPC service class for performing pre-authorization for both MasterCard and NYCE payment networks.
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
public class AdjudicationService extends AplAdjudicationServiceGrpc.AplAdjudicationServiceImplBase{

    private final AuthEngineClientDelegate authEngineClientDelegate;

    private final EventPublisherService eventPublisherService;

    private final CardResponseValidator cardResponseValidator;

    private final FraudResponseValidator fraudResponseValidator;

    private final BASResponseValidator basResponseValidator;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.version}")
    private String version;

    /**
     * Processes the pre-authorization(Adjudication) request by validating the card, account, BAS, and fraud transaction services.
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
     * </ol>
     * </p>
     *
     * @param request          the adjudicationRequest containing the PAN number of the card and other details
     * @param responseObserver the StreamObserver to send the response back to the client
     */
    @Override
    public void processAplAdjudication(AdjudicationRequest request, StreamObserver<AdjudicationResponse> responseObserver){

        if (log.isInfoEnabled()) {
            log.info(ADJ_SERVICE_ENTRY_LOG, request.getTransaction().getID());
        }

        //EventHeader Creation
        EventHeader eventHeader=buildEventHeaderFromAdjudicationModel(request,appName,version);

        publishEvent(eventHeader, EventConstant.ADJUDICATION_MESSAGE_RECEIVED,request.toString());
        try {
            // Scatter: Call Card Service and Fraud Service. Do not gather results at this point

            // 1. Invoke Card Service
            log.debug(CARD_SERVICE_INVOKE_LOG);
            CompletableFuture<com.nationsbenefits.igloo.card.grpc.CardResponse> cardResponse = authEngineClientDelegate.processCardServiceResponse(AuthEngineServiceMapper.buildCardQueryForAPLAdjudication(request));

            // 2. Invoke Transaction Fraud Service
            CompletableFuture<com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse> fraudResponse
                    = authEngineClientDelegate.invokeTransactionFraudService(AuthEngineServiceMapper.buildAnalyzeFraudTxnReqForAPLAdjudication(request));


            // Gather: Combine the results from Card Service, Fraud Service
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(cardResponse, fraudResponse);
            combinedFuture.get();

            if(!cardResponseValidator.validateCardResponseForAdjudication(cardResponse.get(), responseObserver)){
                return;
            }
            log.debug(AUTH_SERVICE_CARD_DETAILS_COMPLETED);

            // 3. Check the Card Service response to retrieve the member ID
            if (Objects.nonNull(cardResponse.get())) {
                if(log.isInfoEnabled()) {
                    log.info(AUTH_SERVICE_CARD_DETAILS_COMPLETED);
                }

                //Publishing ADJUDICATION_CARD_DETAILS_VALIDATED event
                publishEvent(eventHeader, EventConstant.ADJUDICATION_CARD_DETAILS_VALIDATED,cardResponse.get().toString());
            }else{
                publishEvent(eventHeader, EventConstant.ADJUDICATION_CARD_DETAILS_NOT_FOUND,cardResponse.get().toString());
            }

            CompletableFuture<com.nationsbenefits.igloo.member.models.protobuf.MemberResponse> memberResponse = null;
            CompletableFuture<com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse> accountResponse = null;
            // Retrieve the Member Account ID

            String memberAccountId = cardResponse.get().getCard().getMemberAccountId();
            // 4. Invoke the Member Account Service
            accountResponse = authEngineClientDelegate.invokeMemberAccountService(AuthEngineServiceMapper.buildAccountQuery(memberAccountId));

            // 4. Invoke the Member Service
            memberResponse = authEngineClientDelegate.invokeMemberService(AuthEngineServiceMapper.buildMemberQuery(accountResponse.get().getMemberAccount().getMemberID()));

            publishEvent(eventHeader, EventConstant.ADJUDICATION_TRANSACTION_FRAUD_CHECK_SUCCESS,fraudResponse.get().toString());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_MEMBER_DETAILS_VALIDATED,memberResponse.get().toString());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_MEMBER_ACCOUNT_DETAILS_VALIDATED,accountResponse.get().toString());

            if(!fraudResponseValidator.validateFraudResponseForAdjudication(fraudResponse.get(),responseObserver)){
                publishEvent(eventHeader, EventConstant.ADJUDICATION_FRAUD_CHECK_FAILED,fraudResponse.get().toString());
                return;
            }

            // 7. Invoke BAS Service
            com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse analyzeBasketResponse = authEngineClientDelegate.invokeBAS(AuthEngineServiceMapper.buildBASRequestForAPLAdjudication(request, memberResponse.get(), accountResponse.get()));

            if(!basResponseValidator.validateBASResponseForAdjudication(analyzeBasketResponse,responseObserver)) {
                publishEvent(eventHeader, EventConstant.ADJUDICATION_BAS_ADJUDICATION_FAILED,analyzeBasketResponse.toString());
                return;
            }
            publishEvent(eventHeader, EventConstant.ADJUDICATION_BAS_ADJUDICATION_SUCCESS,analyzeBasketResponse.toString());
            AdjudicationResponse response = AuthEngineServiceMapper.buildAdjudicationResponse(analyzeBasketResponse, AdjudicationStatus.newBuilder().setCode(BAS_SUCCESS).setDesc(BAS_SUCCESS_DESC).build());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SUCCESS_RESPONSE_CREATED,response.toString());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            publishEvent(eventHeader, EventConstant.ADJUDICATION_SUCCESS_RESPONSE_SENT,response.toString());

            // Saving transaction for Fraud Velocity Check
            authEngineClientDelegate.invokeSaveTransactionForVelocityCheckService(AuthEngineServiceMapper.buildAnalyzeFraudTxnReqForAPLAdjudication(request));

        } catch (InterruptedException ex) {
            log.error(AUTH_SERVICE_INTERRUPT_EXCEPTION, ex.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_INTERRUPT_EXCEPTION_OCCURRED,ex.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            log.error(AUTH_SERVICE_EXECUTION_EXCEPTION, ex.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SERVICE_EXECUTION_EXCEPTION_OCCURRED,ex.getMessage());
            responseObserver.onError(ex);
        }catch (Exception e) {
            log.error(AUTH_SERVICE_EXCEPTION, e.getMessage());
            publishEvent(eventHeader, EventConstant.ADJUDICATION_SERVICE_EXECUTION_EXCEPTION_OCCURRED,e.getMessage());
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
