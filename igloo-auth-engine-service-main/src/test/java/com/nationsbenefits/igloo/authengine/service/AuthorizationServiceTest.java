package com.nationsbenefits.igloo.authengine.service;

import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.account.grpc.AccountQuery;
import com.nationsbenefits.igloo.authengine.client.AuthEngineClientDelegate;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.ISOMessage;
import com.nationsbenefits.igloo.authengine.validator.BASResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.CardResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.LedgerResponseValidator;
import com.nationsbenefits.igloo.bas.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasket;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.card.grpc.CardQuery;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import com.nationsbenefits.igloo.card.grpc.CardResponseCode;
import com.nationsbenefits.igloo.common.models.grpc.Card;
import com.nationsbenefits.igloo.common.models.grpc.Member;
import com.nationsbenefits.igloo.common.models.grpc.MemberAccount;
import com.nationsbenefits.igloo.common.models.grpc.Transaction;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import com.nationsbenefits.igloo.ledger.grpc.LedgerStatusCode;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionRequest;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionResponse;
import com.nationsbenefits.igloo.ledger.grpc.TransactionStatus;
import com.nationsbenefits.igloo.member.models.protobuf.MemberQuery;
import com.nationsbenefits.igloo.member.models.protobuf.MemberResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionRequest;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**

 *
 * This class used to test the authorizeMethod in  AuthorizationService class
 *
 * @author PwC
 * @version 1.0.0
 * @since 29th Oct 2024
 *
 */
@ExtendWith(SpringExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthEngineClientDelegate authEngineClientDelegate;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private StreamObserver<AuthResponse> responseObserver;

    @Mock
    private EventPublisherService eventPublisherService;

    private AuthRequest authRequest;

    @Mock
    private BASResponseValidator basResponseValidator;

    @Mock
    private CardResponseValidator cardResponseValidator;

    @Mock
    private LedgerResponseValidator ledgerResponseValidator;

    @BeforeEach
    public void setUp() {

        Transaction transData= Transaction.newBuilder().setID("12").build();
        authRequest = AuthRequest.newBuilder()
                .setIsoMessage(ISOMessage.newBuilder()
                        .setTransaction(transData)
                        .build())
                .build();
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_success_pass_ProcessAuthorizationRequest() throws Exception {

        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses

        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("000").build()).build();
        PostTransactionResponse postTransactionResponse = PostTransactionResponse.newBuilder().setTransactionStatus(TransactionStatus.newBuilder().setStatusCode(LedgerStatusCode.LEDGER_TRANSACTION_POSTED).build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any(CardQuery.class))).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any(AnalyzeTransactionRequest.class))).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any(MemberQuery.class))).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any(AccountQuery.class))).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any(AnalyzeBasket.class))).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokePostLedgerTransactionService(any(PostTransactionRequest.class))).thenReturn(postTransactionResponse);
        when(ledgerResponseValidator.validateLedgerResponseForAuthorization(any(),any(),any())).thenReturn(true);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);

        // Verify interactions and responses
        verify(responseObserver, times(1)).onNext(any(AuthResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card details is invalid")
    void test_ProcessAuthorizationRequest_when_card_is_invalid() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_NUMBER_INVALID).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);

        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method when card is not active")
    void test_ProcessAuthorizationRequest_when_card_not_activated() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_NOT_ACTIVATED).build();
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);

        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method when card is expired")
    void test_ProcessAuthorizationRequest_when_card_expired() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_EXPIRED).build();
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses

        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method when CVV mismatch")
    void test_ProcessAuthorizationRequest_when_card_cvn_mismatch() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CVV_MISMATCH).build();
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses

        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method when card type is Debit and status is active")
    void test_ProcessAuthorizationRequest_when_card_authentication_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_AUTHENTICATION_FAILED).build();
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card pin is incorrect")
    void test_ProcessAuthorizationRequest_when_card_pin_validation_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.PIN_VALIDATION_FAILURE).build();
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card status is locked")
    void test_ProcessAuthorizationRequest_when_card_locked_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.LOCKED).build();
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_invalid_merchant() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("206").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_exceed_transaction_limit() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("203").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_insufficient_fund() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("123").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_unauthorized_store_location() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("205").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_products_declined() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("200").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_trans_partially_approved() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("100").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_bas_validation_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("124").build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_ledger_insufficient_fund() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("000").build()).build();
        PostTransactionResponse postTransactionResponse = PostTransactionResponse.newBuilder().setTransactionStatus(TransactionStatus.newBuilder().setStatusCode(LedgerStatusCode.LEDGER_INSUFFICIENT_FUND).build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokePostLedgerTransactionService(any())).thenReturn(postTransactionResponse);
        when(ledgerResponseValidator.validateLedgerResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_ledger_account_not_found() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("000").build()).build();
        PostTransactionResponse postTransactionResponse = PostTransactionResponse.newBuilder().setTransactionStatus(TransactionStatus.newBuilder().setStatusCode(LedgerStatusCode.LEDGER_ACCOUNT_NOT_FOUND).build()).build();
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokePostLedgerTransactionService(any())).thenReturn(postTransactionResponse);
        when(ledgerResponseValidator.validateLedgerResponseForAuthorization(any(),any(),any())).thenReturn(false);
        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
    }
    @Test
    @DisplayName("Testing processAuthorizationRequest method with success")
    void test_ProcessAuthorizationRequest_ledger_verification_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.SUCCESS)
                .setCard(Card.newBuilder().setMemberAccountId("1")).build();
        // Mocking the responses
        AnalyzeTransactionResponse fraudResponse = AnalyzeTransactionResponse.newBuilder().build();
        MemberResponse memberResponse = MemberResponse.newBuilder().build();
        AccountDetailsResponse accountDetailsResponse = AccountDetailsResponse.newBuilder().setMemberAccount(MemberAccount.newBuilder().setMemberID("6").build()).build();
        AnalyzeBasketResponse analyzeBasketResponse = AnalyzeBasketResponse.newBuilder().setStatus(AdjudicationStatus.newBuilder().setCode("000").build()).build();
        PostTransactionResponse postTransactionResponse = PostTransactionResponse.newBuilder().setTransactionStatus(TransactionStatus.newBuilder().setStatusCode(LedgerStatusCode.LEDGER_TRANSACTION_ERROR).build()).build();

        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAuthorization(any(),any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokePostLedgerTransactionService(any())).thenReturn(postTransactionResponse);
        when(ledgerResponseValidator.validateLedgerResponseForAuthorization(any(),any(),any())).thenReturn(false);

        // Call the method under test
        authorizationService.processAuthorizationRequest(authRequest, responseObserver);

        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
        verify(cardResponseValidator, times(1)).validateCardResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokeBAS(any());
        verify(basResponseValidator, times(1)).validateBASResponseForAuthorization(any(),any(),any());
        verify(authEngineClientDelegate,times(1)).invokePostLedgerTransactionService(any());
    }

}