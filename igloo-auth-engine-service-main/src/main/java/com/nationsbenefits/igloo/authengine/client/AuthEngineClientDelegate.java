/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 */
package com.nationsbenefits.igloo.authengine.client;


import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.account.grpc.AccountQuery;
import com.nationsbenefits.igloo.account.grpc.AccountServiceGrpc;
import com.nationsbenefits.igloo.authengine.exception.AuthEngineCircuitBreakerFallbackException;
import com.nationsbenefits.igloo.authengine.exception.AuthEngineExceptionHandler;
import com.nationsbenefits.igloo.authengine.fallback.AuthEngineFallbackHandler;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasket;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.bas.grpc.BasServiceGrpc;
import com.nationsbenefits.igloo.card.grpc.CardQuery;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import com.nationsbenefits.igloo.card.grpc.CardServiceGrpc;
import com.nationsbenefits.igloo.ledger.grpc.LedgerServiceGrpc;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionRequest;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionResponse;
import com.nationsbenefits.igloo.member.models.protobuf.MemberQuery;
import com.nationsbenefits.igloo.member.models.protobuf.MemberResponse;
import com.nationsbenefits.igloo.member.models.protobuf.MemberServiceGrpc;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionRequest;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.SaveTransactionForVelocityCheckResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.TxnFraudAnalyzerServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.BAS_SERVICE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.CARD_SERVICE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.LEDGER_SERVICE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.MEMBER_ACCOUNT_SERVICE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.MEMBER_SERVICE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.TXN_FRAUD_SERVICE;

/**
 * Client Delegate that holds all the service stubs used to invoke other gRPC services.
 * This class provides methods to interact with various gRPC services asynchronously
 * using the scatter-gather design pattern.
 *
 * <p>In the scatter-gather design pattern, multiple requests are sent out in parallel to different services,
 * and the responses are gathered asynchronously. This pattern helps in reducing the overall response time
 * by utilizing parallelism.</p>
 *
 * <p>It includes methods to invoke Card Service, Transaction Fraud Service, Member Service,
 * Member Account Service, Ledger Service, and BAS Service. Each method uses a ManagedChannel
 * to communicate with the respective gRPC service and returns a CompletableFuture to handle
 * the asynchronous response.</p>

 * <p>The class is annotated with {@link Component} to indicate that it is a Spring-managed bean,
 * and {@link Slf4j} for logging purposes.</p>
 *
 * <p>Service addresses are injected using the {@link Value} annotation from the application properties.</p>
 * <p>The Executor used for asynchronous operations is autowired into the class.</p>
 * @author PwC
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthEngineClientDelegate {

    /**
     * The executor used for asynchronous processing of gRPC calls.
     */
    private final Executor grpcThreadPool;

    @Value("${service.deadlineTimeout}")
    private int deadlineTimeout;

    /**
     * The circuitBreaker object
     */
    private CircuitBreaker circuitBreaker;

    /**
     * The CircuitBreakerRegistry Object
     */
    private final CircuitBreakerRegistry registry;

    /**
     * The AuthEngineExceptionHandler object
     */
    private final AuthEngineExceptionHandler authEngineExceptionHandler;

    /**
     * The authEngineFallbackHandler object
     */
    private final AuthEngineFallbackHandler authEngineFallbackHandler;

    /**
     * The cardServiceChannel object
     */
    private final ManagedChannel cardServiceChannel;

    /**
     * The txnFraudAnalyzerServiceChannel object
     */
    private final ManagedChannel txnFraudAnalyzerServiceChannel;

    /**
     * The memberServiceChannel object
     */
    private final ManagedChannel memberServiceChannel;

    /**
     * The memberAccountServiceChannel object
     */
    private final ManagedChannel memberAccountServiceChannel;

    /**
     * The basServiceChannel object
     */
    private final ManagedChannel basServiceChannel;

    /**
     * The ledgerServiceChannel object
     */
    private final ManagedChannel ledgerServiceChannel;

    /**
     * The retry object
     */
    private final Retry retry;



    /**
     *
     * This method is used to invoke Card service to fetch card details.
     * The service stub is invoked asynchronously using a CompletableFuture.
     * The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     * @param cardQuery contains card details needed for the query.
     * @return CompletableFuture<CardDetails> which contains the card details.
     */

    public CompletableFuture<CardResponse> processCardServiceResponse(CardQuery cardQuery) {
        // Initialize the Circuit Breaker
       this.circuitBreaker = registry.circuitBreaker(CARD_SERVICE);

        // Create a Supplier for the Card Service gRPC call
        Supplier<CardResponse> cardResponseSupplier = () -> {
            CardServiceGrpc.CardServiceBlockingStub cardServiceStub = CardServiceGrpc.newBlockingStub(cardServiceChannel);
            CardResponse cardResponse;
            try {
                cardResponse = cardServiceStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).validateAndFetchCardDetails(cardQuery);
            } catch (StatusRuntimeException e) {
                log.error("Exception thrown from Card Service server : {} : {}", e.getStatus().getCode(), e.getStatus().getDescription());
                authEngineExceptionHandler.handleRuntimeException(e);
                throw new AuthEngineCircuitBreakerFallbackException("Exception processing Card Service Response");
            }
            return cardResponse;
        };

        // Decorate the supplier with Circuit Breaker, Retry, and Fallback
        Supplier<CardResponse> decoratedSupplier = Decorators.ofSupplier(cardResponseSupplier)
               .withCircuitBreaker(circuitBreaker)
               .withRetry(retry)
               .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessCardServiceResponse(cardQuery)).decorate();

        // Execute the decorated supplier asynchronously
        return CompletableFuture.supplyAsync(decoratedSupplier, grpcThreadPool);
    }


    /**
     *  This method is used to invoke the Transaction Fraud service to validate a transaction for fraud.
     *  The service stub is invoked asynchronously using a CompletableFuture.
     *  The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     *  @param analyzeTransactionRequest contains the transaction details needed for fraud analysis.
     *  @return CompletableFuture<AnalyzeTransactionResponse> which contains the response of the fraud analysis.
     */

    public CompletableFuture<AnalyzeTransactionResponse> invokeTransactionFraudService(AnalyzeTransactionRequest analyzeTransactionRequest) {
        // Initialize the Circuit Breaker
       this.circuitBreaker = registry.circuitBreaker(TXN_FRAUD_SERVICE); // put in constant

        // Create a Supplier for the Card Service gRPC call
        Supplier<AnalyzeTransactionResponse> analyzeTransactionResponseSupplier = () -> {
            TxnFraudAnalyzerServiceGrpc.TxnFraudAnalyzerServiceBlockingStub txnFraudAnalyzerServiceBlockingStub = TxnFraudAnalyzerServiceGrpc.newBlockingStub(txnFraudAnalyzerServiceChannel);
            AnalyzeTransactionResponse analyzeTransactionResponse;
            try {
                analyzeTransactionResponse = txnFraudAnalyzerServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).analyzeTransaction(analyzeTransactionRequest);
            } catch (StatusRuntimeException e) {
                log.error("Exception thrown from Analyze Txn Fraud Service server : {} : {}", e.getStatus().getCode(), e.getStatus().getDescription());
                authEngineExceptionHandler.handleRuntimeException(e);
                throw new AuthEngineCircuitBreakerFallbackException("Exception processing Txn Fraud Service Response");
            }
            return analyzeTransactionResponse;
        };

        // Decorate the supplier with Circuit Breaker, Retry, and Fallback
        Supplier<AnalyzeTransactionResponse> decoratedSupplier = Decorators.ofSupplier(analyzeTransactionResponseSupplier)
              .withCircuitBreaker(circuitBreaker)
              .withRetry(retry)
              .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessTxnFraudServiceResponse(analyzeTransactionRequest)).decorate();

        // Execute the decorated supplier asynchronously
        return CompletableFuture.supplyAsync(decoratedSupplier, grpcThreadPool);
    }


    /**
     * Invokes the Fraud Service to save a transaction for velocity checks, leveraging a Circuit Breaker
     * and Retry mechanism to ensure fault tolerance and reliability.
     *
     * <p>This method is responsible for integrating with the Fraud Service's gRPC endpoint to analyze
     * transactions and save them for velocity checks. It employs a Circuit Breaker for managing
     * service unavailability and retries in case of transient failures. A fallback mechanism is also
     * included to handle errors gracefully.</p>
     *
     * @param analyzeTransactionRequest the request object containing transaction details that need to
     *                                  be saved for velocity checks.
     *
     * @return an instance of {@link SaveTransactionForVelocityCheckResponse} containing the response
     *         from the Fraud Service after successfully processing the request.
     *
     * @throws AuthEngineCircuitBreakerFallbackException if the Circuit Breaker trips or any non-retryable
     *         exception occurs while communicating with the Fraud Service.
     *
     * <p>Implementation Details:</p>
     * <ul>
     *   <li>Logs the initiation of the transaction saving process for fraud velocity checks.</li>
     *   <li>Uses a gRPC blocking stub to call the `saveTransactionForVelocityCheck` method of the Fraud Service,
     *       with a configurable timeout for the request.</li>
     *   <li>Handles runtime exceptions such as {@link io.grpc.StatusRuntimeException} by logging the error,
     *       invoking an exception handler, and rethrowing a custom exception.</li>
     *   <li>Decorates the gRPC call with Circuit Breaker, Retry, and Fallback mechanisms:
     *       <ul>
     *         <li>Circuit Breaker: Prevents calls to the Fraud Service if it is detected as unavailable.</li>
     *         <li>Retry: Attempts to re-execute the call in case of transient failures.</li>
     *         <li>Fallback: Processes a fallback response using a handler if all retries fail.</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>Dependencies:</p>
     * <ul>
     *   <li>gRPC channel: Used to communicate with the Fraud Service.</li>
     *   <li>{@code circuitBreaker}: Circuit Breaker instance to manage fault tolerance.</li>
     *   <li>{@code retry}: Retry policy for handling transient failures.</li>
     *   <li>{@code authEngineExceptionHandler}: Handles runtime exceptions thrown by the Fraud Service.</li>
     *   <li>{@code authEngineFallbackHandler}: Processes fallback responses when all attempts fail.</li>
     * </ul>
     */
    public SaveTransactionForVelocityCheckResponse invokeSaveTransactionForVelocityCheckService(AnalyzeTransactionRequest analyzeTransactionRequest) {
        log.info("Saving Transaction for Fraud Velocity Check") ;
        this.circuitBreaker = registry.circuitBreaker("Fraud Service");

        TxnFraudAnalyzerServiceGrpc.TxnFraudAnalyzerServiceBlockingStub txnFraudAnalyzerServiceBlockingStub = TxnFraudAnalyzerServiceGrpc.newBlockingStub(txnFraudAnalyzerServiceChannel);
        Decorators.DecorateSupplier<SaveTransactionForVelocityCheckResponse> decoratedSupplier = Decorators.ofSupplier(() -> {
            SaveTransactionForVelocityCheckResponse saveTransactionForVelocityCheckResponse = null;
            try {
                saveTransactionForVelocityCheckResponse = txnFraudAnalyzerServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).saveTransactionForVelocityCheck(analyzeTransactionRequest);
            } catch (StatusRuntimeException e) {
                log.error("Exception thrown from Fraud service server : {} : {}",
                        e.getStatus().getCode(), e.getStatus().getDescription());
                authEngineExceptionHandler.handleRuntimeException(e);
                throw new AuthEngineCircuitBreakerFallbackException("Exception processing Fraud Service Response");
            }
            return saveTransactionForVelocityCheckResponse;
        }).withCircuitBreaker(circuitBreaker).withRetry(retry)
        .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessFraudSericeSaveTxnResponse(analyzeTransactionRequest));

        return decoratedSupplier.get();
    }


    /**
     * This method is used to invoke Member service to retrieve the member details
     *  The service stub is invoked asynchronously using a CompletableFuture.
     *  The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     * @param memberQuery contains member request
     * @return CompletableFuture<MemberResponse> which contains Member details
     */
    public CompletableFuture<MemberResponse> invokeMemberService(MemberQuery memberQuery) {
        // Initialize the Circuit Breaker
       this.circuitBreaker = registry.circuitBreaker(MEMBER_SERVICE);

        // Create a Supplier for the Card Service gRPC call
        Supplier<MemberResponse> memberResponseSupplier = () -> {
            MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub = MemberServiceGrpc.newBlockingStub(memberServiceChannel);
            MemberResponse memberResponse;
            try {
                memberResponse = memberServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).retrieveMemberDetails(memberQuery);
            } catch (StatusRuntimeException e) {
                log.error("Exception thrown from Member Service server : {} : {}", e.getStatus().getCode(), e.getStatus().getDescription());
                authEngineExceptionHandler.handleRuntimeException(e);
                throw new AuthEngineCircuitBreakerFallbackException("Exception processing Member Service Response");
            }
            return memberResponse;
        };

        // Decorate the supplier with Circuit Breaker, Retry, and Fallback
        Supplier<MemberResponse> decoratedSupplier = Decorators.ofSupplier(memberResponseSupplier)
               .withCircuitBreaker(circuitBreaker)
               .withRetry(retry)
               .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessMemberServiceResponse(memberQuery)).decorate();

        // Execute the decorated supplier asynchronously
        return CompletableFuture.supplyAsync(decoratedSupplier, grpcThreadPool);
    }

    /**
     *  This method is used to invoke the Member Account service to retrieve member account details.
     *  The service stub is invoked asynchronously using a CompletableFuture.
     *  The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     *   @param accountQuery contains the account request details.
     *   @return CompletableFuture<AccountDetailsResponse> which contains the account details.
     */
    public CompletableFuture<AccountDetailsResponse> invokeMemberAccountService(AccountQuery accountQuery) {
        // Initialize the Circuit Breaker
      this.circuitBreaker = registry.circuitBreaker(MEMBER_ACCOUNT_SERVICE);

        // Create a Supplier for the Card Service gRPC call
        Supplier<AccountDetailsResponse> accountDetailsResponseSupplier = () -> {
            AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub = AccountServiceGrpc.newBlockingStub(memberAccountServiceChannel);
            AccountDetailsResponse accountDetailsResponse;
            try {
                accountDetailsResponse = accountServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).retrieveMemberAccountDetails(accountQuery);
            } catch (StatusRuntimeException e) {
                log.error("Exception thrown from Member Account Service server : {} : {}", e.getStatus().getCode(), e.getStatus().getDescription());
                authEngineExceptionHandler.handleRuntimeException(e);
                throw new AuthEngineCircuitBreakerFallbackException("Exception processing Member Account Service Response");
            }
            return accountDetailsResponse;
        };

        // Decorate the supplier with Circuit Breaker, Retry, and Fallback
        Supplier<AccountDetailsResponse> decoratedSupplier = Decorators.ofSupplier(accountDetailsResponseSupplier)
              .withCircuitBreaker(circuitBreaker)
              .withRetry(retry)
              .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessAccountServiceResponse(accountQuery)).decorate();

        // Execute the decorated supplier asynchronously
        return CompletableFuture.supplyAsync(decoratedSupplier, grpcThreadPool);
    }

    /**
     * This method is used to invoke the Ledger service to post a transaction to the ledger.
     * The service stub is invoked asynchronously using a CompletableFuture.
     * The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     *   @param postTransactionRequest contains the transaction details to be posted to the ledger.
     *     @return PostTransactionResponse which contains the response from the ledger service.
     */

    public PostTransactionResponse invokePostLedgerTransactionService(PostTransactionRequest postTransactionRequest) {

       this.circuitBreaker = registry.circuitBreaker(LEDGER_SERVICE);

        LedgerServiceGrpc.LedgerServiceBlockingStub ledgerServiceBlockingStub = LedgerServiceGrpc.newBlockingStub(ledgerServiceChannel);
        Decorators.DecorateSupplier<PostTransactionResponse> decoratedSupplier = Decorators.ofSupplier(() -> {
                    PostTransactionResponse postTransactionResponse;
                    try {
                        postTransactionResponse = ledgerServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).postTransaction(postTransactionRequest);
                    } catch (StatusRuntimeException e) {
                        log.error("Exception thrown from Ledger service server : {} : {}",
                                e.getStatus().getCode(), e.getStatus().getDescription());
                        authEngineExceptionHandler.handleRuntimeException(e);
                        throw new AuthEngineCircuitBreakerFallbackException("Exception processing Ledger Service Response");
                    }
                    return postTransactionResponse;
                }).withCircuitBreaker(circuitBreaker).withRetry(retry)
                .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessLedgerServiceResponse(postTransactionRequest));

        return decoratedSupplier.get();
    }

    /**
     *  This method is used to invoke the BAS (Basket Analysis Service) to retrieve the result of BAS analysis.
     *  The method is invoked synchronously.
     *  The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     *
     *   @param analyzeBasket contains the BAS request details.
     *   @return AnalyzeBasketResponse which contains the response from the BAS service.
     *
     */
    public AnalyzeBasketResponse invokeBAS(AnalyzeBasket analyzeBasket) {
        if(log.isInfoEnabled()) {
            log.info("Invoking BAS service..");
        }
       this.circuitBreaker = registry.circuitBreaker(BAS_SERVICE);

        BasServiceGrpc.BasServiceBlockingStub basServiceBlockingStub = BasServiceGrpc.newBlockingStub(basServiceChannel);
        Decorators.DecorateSupplier<AnalyzeBasketResponse> decoratedSupplier = Decorators.ofSupplier(() -> {
                    AnalyzeBasketResponse analyzeBasketResponse;
                    try {
                        analyzeBasketResponse = basServiceBlockingStub.withDeadlineAfter(deadlineTimeout, TimeUnit.MILLISECONDS).analyzeBasket(analyzeBasket);
                    } catch (StatusRuntimeException e) {
                        log.error("Exception thrown from BAS service server : {} : {}",
                                e.getStatus().getCode(), e.getStatus().getDescription());
                        authEngineExceptionHandler.handleRuntimeException(e);
                        throw new AuthEngineCircuitBreakerFallbackException("Exception processing BAS Service Response");
                    }
                    return analyzeBasketResponse;
                }).withCircuitBreaker(circuitBreaker).withRetry(retry)
                .withFallback(throwable -> authEngineFallbackHandler.fallbackProcessBASServiceResponse(analyzeBasket));

        return decoratedSupplier.get();
    }

}
