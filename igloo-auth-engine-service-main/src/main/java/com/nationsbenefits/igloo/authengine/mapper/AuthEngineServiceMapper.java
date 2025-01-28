/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine.mapper;

import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.account.grpc.AccountQuery;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseCode;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponseStatus;
import com.nationsbenefits.igloo.authengine.grpc.ProductAdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.ProductAdjudicationStatus;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasket;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.card.grpc.CardQuery;
import com.nationsbenefits.igloo.card.grpc.CardValidation;
import com.nationsbenefits.igloo.common.models.grpc.Card;
import com.nationsbenefits.igloo.ledger.grpc.MemberAccount;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionRequest;
import com.nationsbenefits.igloo.member.models.protobuf.MemberQuery;
import com.nationsbenefits.igloo.member.models.protobuf.MemberResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionRequest;
import org.springframework.util.StringUtils;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationRequest;

import java.util.List;
import java.util.Objects;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.*;

/**
 * Mapper class used to build request objects for gRPC services and process responses.
 * This class provides static methods to construct various gRPC request objects and
 * responses required by different services in the Auth Engine.
 *
 * <p>The class includes methods to build requests for Card Service, Transaction Fraud Service,
 * Member Service, Account Service, BAS Service, and Post Ledger Transaction. It also includes
 * a method to build the Auth Response.</p>
 *
 * <p>Using a dedicated mapper class helps in maintaining a clear separation of concerns,
 * making the code more modular and easier to maintain.</p>
 *
 * <p>The class is designed to be non-instantiable by having a private constructor,
 * ensuring that it only serves as a container for static methods.</p>
 *
 * @author PwC
 */
public class AuthEngineServiceMapper {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AuthEngineServiceMapper() {
        // Prevent instantiation
    }

    /**
     * Builds the Card service request.
     *
     * <p>This method constructs a {@link CardQuery} object based on the provided {@link AuthRequest}.
     * It uses the PAN hash from the ISO message's card details.</p>
     *
     * @param request the AuthRequest containing the necessary details
     * @return a {@link CardQuery} object, or null if the request is invalid
     */
    public static CardQuery buildCardQuery(AuthRequest request) {
        CardQuery cardQuery = null;
        if (Objects.nonNull(request)) {
            cardQuery = CardQuery.newBuilder().setBy(PAN).setValue(request.getIsoMessage().getCard().getPanHash())
                .setCardValidation(CardValidation.newBuilder().setEncryptedCVV(request.getIsoMessage().getCard().getCvv()).build()).build();
        }
        return cardQuery;
    }

    /**
     * Builds the Transaction Fraud Service request.
     *
     * <p>This method constructs an {@link AnalyzeTransactionRequest} object based on the provided {@link AuthRequest}.
     * It includes the card details and transaction data from the ISO message.</p>
     *
     * @param request the AuthRequest containing the necessary details
     * @return an {@link AnalyzeTransactionRequest} object, or null if the request is invalid
     */
    public static AnalyzeTransactionRequest buildAnalyzeFraudTxnReq(AuthRequest request) {
        AnalyzeTransactionRequest analyzeTransactionRequest = null;
        if (Objects.nonNull(request)) {
            analyzeTransactionRequest = AnalyzeTransactionRequest.newBuilder()
                    .setCard(request.getIsoMessage().getCard())
                    .setTransaction(request.getIsoMessage().getTransaction())
                    .build();
        }
        return analyzeTransactionRequest;
    }

    /**
     * Builds the Member service request.
     *
     * <p>This method constructs a {@link MemberQuery} object based on the provided member ID.</p>
     *
     * @param memberId the member ID
     * @return a {@link MemberQuery} object, or null if the member ID is empty
     */
    public static MemberQuery buildMemberQuery(String memberId) {
        MemberQuery memberQuery = null;
        if (!StringUtils.isEmpty(memberId)) {
            memberQuery = MemberQuery.newBuilder().setBy(MEMBER_ID).setValue(memberId).build();
        }
        return memberQuery;
    }

    /**
     * Builds the Account service request.
     *
     * <p>This method constructs an {@link AccountQuery} object based on the provided member ID.</p>
     *
     * @param memberAccountId the member ID
     * @return an {@link AccountQuery} object, or null if the member ID is empty
     */
    public static AccountQuery buildAccountQuery(String memberAccountId) {
        AccountQuery accountQuery = null;
        if (!StringUtils.isEmpty(memberAccountId)) {
            accountQuery = AccountQuery.newBuilder().setBy(MEMBER_ACCOUNT_ID).setValue(memberAccountId).build();
        }
        return accountQuery;
    }

    /**
     * Builds the BAS service request object.
     *
     * <p>This method constructs an {@link AnalyzeBasket} object based on the provided {@link AuthRequest},
     * {@link MemberResponse}, and {@link AccountDetailsResponse}.</p>
     *
     * @param request the AuthRequest containing the necessary details
     * @param memberResponse the MemberResponse containing member details
     * @param accountDetailsResponse the AccountDetailsResponse containing account details
     * @return an {@link AnalyzeBasket} object, or null if any of the parameters are invalid
     */
    public static AnalyzeBasket buildBASRequest(AuthRequest request, MemberResponse memberResponse, AccountDetailsResponse accountDetailsResponse) {
        AnalyzeBasket analyzeBasket = null;
        if (Objects.nonNull(request) && Objects.nonNull(memberResponse) && Objects.nonNull(accountDetailsResponse)) {
            analyzeBasket = AnalyzeBasket.newBuilder()
                    .setCard(Card.newBuilder().setPanHash(request.getIsoMessage().getCard().getPanHash()))
                    .setMember(memberResponse.getMember())
                    .setAccount(accountDetailsResponse.getMemberAccount().getAccount())
                    .setTransaction(request.getIsoMessage().getTransaction())
                    .setMerchant(request.getIsoMessage().getMerchant())
                    .build();
        }
        return analyzeBasket;
    }

    /**
     * Builds the Post Ledger Transaction request.
     *
     * <p>This method constructs a {@link PostTransactionRequest} object based on the provided
     * {@link AccountDetailsResponse} and {@link AdjudicationResponse}.</p>
     *
     * @param account the AccountDetailsResponse containing account details
     * @param basketResponse the AnalyzeBasketResponse containing basket analysis details
     * @return a {@link PostTransactionRequest} object, or null if any of the parameters are invalid
     */
    public static PostTransactionRequest buildPostTransactionRequest(AccountDetailsResponse account, AnalyzeBasketResponse basketResponse) {
        PostTransactionRequest postTransactionRequest = null;
        if (Objects.nonNull(account) && Objects.nonNull(basketResponse)) {
            postTransactionRequest = PostTransactionRequest.newBuilder()
                    .setMemberAccount(MemberAccount.newBuilder()
                            .setAccount(account.getMemberAccount().getAccount())
                            .addAllPurseAccounts(basketResponse.getPurseAccountsList())
                            .build())
                    .setTransaction(basketResponse.getTransaction())
                    .build();
        }
        return postTransactionRequest;
    }

    /**
     * Builds the Auth response.
     *
     * <p>This method constructs an {@link AuthResponse} object based on the provided response code,
     * response description, and {@link AuthRequest}.</p>
     *
     * @param authResponseCode the response code
     * @param responseDescription the response description
     * @param request the AuthRequest containing the necessary details
     * @return an {@link AuthResponse} object
     */
    public static AuthResponse buildAuthResponse(String responseDescription, AuthRequest request,AuthResponseCode authResponseCode) {
        return AuthResponse.newBuilder().setStatusCode(authResponseCode.toString())
                .setIsoMessage(request.getIsoMessage())
                .setResponseCode(AuthResponseStatus.newBuilder().setAuthResponseCode(authResponseCode).setResponseDescription(responseDescription).build()).build();
    }


    /**
     * Builds the Card service request from Adjudication request.
     *
     * <p>This method constructs a {@link CardQuery} object based on the provided {@link AdjudicationRequest}.
     * It uses the PAN hash from the adjudication request.</p>
     *
     * @param request the AdjudicationRequest containing the necessary details
     * @return a {@link CardQuery} object, or null if the request is invalid
     */
    public static CardQuery buildCardQueryForAPLAdjudication(AdjudicationRequest request) {
        CardQuery cardQuery = null;
        if (Objects.nonNull(request.getCard())) {
            cardQuery = CardQuery.newBuilder().setBy(PAN).setValue(request.getCard().getPanHash()).setCardValidation(CardValidation.newBuilder().setEncryptedCVV(request.getCard().getCvv()).build()).build();
        }
        return cardQuery;
    }

    /**
     * Builds the Transaction Fraud Service request from Adjudication request.
     *
     * <p>This method constructs an {@link AnalyzeTransactionRequest} object based on the provided {@link AdjudicationRequest}.
     * It includes the card details and transaction data from adjudication request.</p>
     *
     * @param request the AdjudicationRequest containing the necessary details
     * @return an {@link AnalyzeTransactionRequest} object, or null if the request is invalid
     */
    public static AnalyzeTransactionRequest buildAnalyzeFraudTxnReqForAPLAdjudication(AdjudicationRequest request) {
        AnalyzeTransactionRequest analyzeTransactionRequest = null;
        if (Objects.nonNull(request)) {
            analyzeTransactionRequest = AnalyzeTransactionRequest.newBuilder()
                    .setCard(request.getCard())
                    .setTransaction(request.getTransaction())
                    .build();
        }
        return analyzeTransactionRequest;
    }

    /**
     * Builds the BAS service request object for APL Adjudication.
     *
     * <p>This method constructs an {@link AnalyzeBasket} object based on the provided {@link AdjudicationRequest},
     * {@link MemberResponse}, and {@link AccountDetailsResponse}.</p>
     *
     * @param request the AdjudicationRequest containing the necessary details
     * @param memberResponse the MemberResponse containing member details
     * @param accountDetailsResponse the AccountDetailsResponse containing account details
     * @return an {@link AnalyzeBasket} object, or null if any of the parameters are invalid
     */
    public static AnalyzeBasket buildBASRequestForAPLAdjudication(AdjudicationRequest request, MemberResponse memberResponse, AccountDetailsResponse accountDetailsResponse) {
        AnalyzeBasket analyzeBasket = null;
        if (Objects.nonNull(request) && Objects.nonNull(memberResponse) && Objects.nonNull(accountDetailsResponse)) {
            analyzeBasket = AnalyzeBasket.newBuilder()
                    .setMember(memberResponse.getMember())
                    .setAccount(accountDetailsResponse.getMemberAccount().getAccount())
                    .setTransaction(request.getTransaction())
                    .setMerchant(request.getMerchant())
                    .setCard(Card.newBuilder().setPanHash(request.getCard().getPanHash()))
                    .build();
        }
        return analyzeBasket;
    }

    /**
     * Building AdjudicationResponse object
     * @param adjudicationStatus
     * @return
     */
    public static AdjudicationResponse buildAdjudicationForCard(AdjudicationStatus adjudicationStatus){
        AdjudicationResponse adjudicationResponse;
        adjudicationResponse = AdjudicationResponse.newBuilder()
            .setStatus(AdjudicationStatus.newBuilder().setCode(adjudicationStatus.getCode()).setDesc(adjudicationStatus.getDesc()).build()).build();
        return adjudicationResponse;
    }



    public static AdjudicationResponse buildAdjudicationResponseForFraud(AdjudicationStatus adjudicationStatus){
        AdjudicationResponse adjudicationResponse;
        adjudicationResponse = AdjudicationResponse.newBuilder()
                .setStatus(AdjudicationStatus.newBuilder().setCode(adjudicationStatus.getCode()).setDesc(adjudicationStatus.getDesc()).build()).build();
        return adjudicationResponse;
    }

    /**
     * Builds the Adjudication response.
     *
     * <p>This method constructs an {@link AdjudicationResponse} object based on the provided response code,
     * response description, and {@link AnalyzeBasketResponse}.</p>
     *
     * @param adjudicationStatus the response code
     * @param analyzeBasketResponse the AnalyzeBasketResponse containing the necessary details
     * @return an {@link AuthResponse} object
     */
    public static AdjudicationResponse buildAdjudicationResponse(AnalyzeBasketResponse analyzeBasketResponse, AdjudicationStatus adjudicationStatus) {
        AdjudicationResponse adjudicationResponse = null;
        try {
            adjudicationResponse = AdjudicationResponse.newBuilder()
                .setTransaction(analyzeBasketResponse.getTransaction())
                .setAuthorizedTransactionAmount(analyzeBasketResponse.getAuthorizedTransactionAmount())
                .setNationsBenefitsTransactionId(analyzeBasketResponse.getNationsBenefitsGeneratedId())
                .setStatus(AdjudicationStatus.newBuilder().setCode(analyzeBasketResponse.getStatus().getCode()).setDesc(analyzeBasketResponse.getStatus().getDesc()).build())
                .addAllProductAdjudicationResponses(buildProductAdjudicationResponses(analyzeBasketResponse.getProductAdjudicationResponsesList()))
                .addAllPurseAccounts(analyzeBasketResponse.getPurseAccountsList())
                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return adjudicationResponse;

    }

    /**
     * Builds a list of {@code ProductAdjudicationResponse} objects by transforming a list of gRPC {@code ProductAdjudicationResponse} objects.
     * <p>
     * This method maps each {@code com.nationsbenefits.igloo.bas.grpc.ProductAdjudicationResponse} from the input list
     * to a {@code ProductAdjudicationResponse}, copying relevant fields and evaluating the authorization status.
     * The authorization status is determined by the {@code evaluateAuthStatus} method based on the status code.
     * </p>
     *
     * @param productAdjudicationResponseList the list of gRPC {@code ProductAdjudicationResponse} objects to transform
     * @return a list of {@code ProductAdjudicationResponse} objects with populated fields
     */
    public static List<ProductAdjudicationResponse> buildProductAdjudicationResponses(java.util.List<com.nationsbenefits.igloo.bas.grpc.ProductAdjudicationResponse> productAdjudicationResponseList) {
        return productAdjudicationResponseList.stream().map(productAdjudication ->
            ProductAdjudicationResponse.newBuilder().setProduct(productAdjudication.getProduct())
                    .setAuthResult(productAdjudication.getAuthResult())
                    .setAuthorizedTax(productAdjudication.getAuthorizedTax())
                    .setPurseType(productAdjudication.getPurseType())
                    .setAuthorizedFees(productAdjudication.getAuthorizedFees())
                    .setStatus(ProductAdjudicationStatus.newBuilder().setCode(productAdjudication.getStatus().getCode()).setDesc(productAdjudication.getStatus().getDesc()).build())
                    .setAuthResult(evaluateAuthStatus(productAdjudication.getStatus().getCode()))
                    .setAuthorizedAmountBeforeTax(productAdjudication.getAuthorizedAmountBeforeTax()).build()).toList();
    }


    /**
     * Evaluates the authorization status based on the provided product adjudication code.
     * <p>
     * If the product adjudication code is "100", the method returns "APPROVED".
     * For any other non-empty adjudication code, it returns "DECLINED".
     * If the adjudication code is empty or null, it returns an empty string.
     * </p>
     *
     * @param productAdjudicationCode the adjudication code representing the product's authorization result
     * @return the authorization status as a String: "APPROVED", "DECLINED", or an empty string if input is empty or null
     */
    public static String evaluateAuthStatus(String productAdjudicationCode) {
        String authStatus = "";
        if(!StringUtils.isEmpty(productAdjudicationCode)){
            switch (productAdjudicationCode){
                case BAS_TRANSACTION_PARTIALLY_APPROVED -> authStatus = APPROVED_AUTH_RESPONSE;
                default -> {
                    authStatus = DECLINED_AUTH_RESPONSE;
                }
            }
        }
        return authStatus;
    }

}
