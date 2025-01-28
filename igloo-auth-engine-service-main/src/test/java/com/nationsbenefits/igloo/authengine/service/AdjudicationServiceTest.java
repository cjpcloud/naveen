package com.nationsbenefits.igloo.authengine.service;

import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.account.grpc.AccountQuery;
import com.nationsbenefits.igloo.authengine.client.AuthEngineClientDelegate;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationRequest;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.validator.BASResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.CardResponseValidator;
import com.nationsbenefits.igloo.authengine.validator.FraudResponseValidator;
import com.nationsbenefits.igloo.bas.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasket;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.card.grpc.CardQuery;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import com.nationsbenefits.igloo.card.grpc.CardResponseCode;
import com.nationsbenefits.igloo.common.models.grpc.*;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
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
 * This class used to test AdjudicationService the class
 *
 * @author PwC
 * @version 1.0.0
 * @since 13th Nov 2024
 *
 */
@ExtendWith(SpringExtension.class)
class AdjudicationServiceTest {

    @InjectMocks
    private AdjudicationService adjudicationService;

    @Mock
    private StreamObserver<AdjudicationResponse> responseObserver;

    @Mock
    private EventPublisherService eventPublisherService;

    private AdjudicationRequest adjudicationRequest;

    @Mock
    private AuthEngineClientDelegate authEngineClientDelegate;

    @Mock
    private BASResponseValidator basResponseValidator;

    @Mock
    private CardResponseValidator cardResponseValidator;

    @Mock
    private FraudResponseValidator fraudResponseValidator;


    @BeforeEach
    public void setUp() {

        Transaction transData= Transaction.newBuilder().setID("12").build();
        adjudicationRequest = AdjudicationRequest.newBuilder()
            .setCard(Card.newBuilder().setPanHash("98f83ko0smb").build())
            .setTransaction(transData)
            .setMerchant(Merchant.newBuilder().setCategoryCode("5912").setID("678292001").build())
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
        when(authEngineClientDelegate.processCardServiceResponse(any(CardQuery.class))).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any(AnalyzeTransactionRequest.class))).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any(MemberQuery.class))).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any(AccountQuery.class))).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any(AnalyzeBasket.class))).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(true);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);

        // Verify interactions and responses
        verify(responseObserver, times(1)).onNext(any(AdjudicationResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card details is invalid")
    void test_ProcessAuthorizationRequest_when_card_is_invalid() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_NUMBER_INVALID).build();
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
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
        // Call the method under test
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);

        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);

        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card type is Debit and status is active")
    void test_ProcessAuthorizationRequest_when_card_authentication_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.CARD_AUTHENTICATION_FAILED).build();
        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);

        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card pin is incorrect")
    void test_ProcessAuthorizationRequest_when_card_pin_validation_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.PIN_VALIDATION_FAILURE).build();
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);

        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

    @Test
    @DisplayName("Testing processAuthorizationRequest method when card status is locked")
    void test_ProcessAuthorizationRequest_when_card_locked_failed() throws Exception {
        Member member= Member.newBuilder().setID("12").build();
        CardResponse cardDetails= CardResponse.newBuilder().setCardStatus(CardResponseCode.LOCKED).build();
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(false);

        // Mocking the responses
        when(authEngineClientDelegate.processCardServiceResponse(any())).thenReturn(CompletableFuture.completedFuture(cardDetails));
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeTransactionFraudService(any());
        verify(fraudResponseValidator, times(1)).validateFraudResponseForAdjudication(any(), any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeTransactionFraudService(any());
        verify(fraudResponseValidator, times(1)).validateFraudResponseForAdjudication(any(), any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeBAS(any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeBAS(any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeBAS(any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).invokeBAS(any());
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
        when(cardResponseValidator.validateCardResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeTransactionFraudService(any())).thenReturn(CompletableFuture.completedFuture(fraudResponse));
        when(fraudResponseValidator.validateFraudResponseForAdjudication(any(),any())).thenReturn(true);
        when(authEngineClientDelegate.invokeMemberService(any())).thenReturn(CompletableFuture.completedFuture(memberResponse));
        when(authEngineClientDelegate.invokeMemberAccountService(any())).thenReturn(CompletableFuture.completedFuture(accountDetailsResponse));
        when(authEngineClientDelegate.invokeBAS(any())).thenReturn(analyzeBasketResponse);
        when(basResponseValidator.validateBASResponseForAdjudication(any(),any())).thenReturn(false);
        // Call the method under test
        adjudicationService.processAplAdjudication(adjudicationRequest, responseObserver);
        // Verify interactions and responses
        verify(authEngineClientDelegate, times(1)).processCardServiceResponse(any());
    }

}
