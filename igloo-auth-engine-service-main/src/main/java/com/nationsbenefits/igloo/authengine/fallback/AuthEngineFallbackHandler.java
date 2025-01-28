package com.nationsbenefits.igloo.authengine.fallback;

import com.nationsbenefits.igloo.account.grpc.AccountDetailsResponse;
import com.nationsbenefits.igloo.account.grpc.AccountQuery;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasket;
import com.nationsbenefits.igloo.bas.grpc.AnalyzeBasketResponse;
import com.nationsbenefits.igloo.card.grpc.CardQuery;
import com.nationsbenefits.igloo.card.grpc.CardResponse;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionRequest;
import com.nationsbenefits.igloo.ledger.grpc.PostTransactionResponse;
import com.nationsbenefits.igloo.member.models.protobuf.MemberQuery;
import com.nationsbenefits.igloo.member.models.protobuf.MemberResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionRequest;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.SaveTransactionForVelocityCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author PwC
 *
 * Fallback handler for Auth Engine Service.
 * This class handles fallback processing in-case there is an exception in processing the downstream service response
 *
 */
@Slf4j
@Component
public class AuthEngineFallbackHandler {

    /**
     * Fallback method for processing the Card Service response.
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param cardQuery the card query object containing the necessary data for querying card details.
     * @return a default CardResponse instance as a fallback.
     */
    public CardResponse fallbackProcessCardServiceResponse(CardQuery cardQuery) {
        log.info("Inside Fallback processor for Card Service Response");
        /**
         * TODO: Implement stand-in processing for Card Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return CardResponse.newBuilder().build();
    }

    /**
     * Fallback method for processing the Fraud Transaction Service response.
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param analyzeTransactionRequest the AnalyzeTransactionRequest object containing the necessary data for analyzing transaction fraud
     * @return a default AnalyzeTransactionResponse instance as a fallback.
     */
    public AnalyzeTransactionResponse fallbackProcessTxnFraudServiceResponse(AnalyzeTransactionRequest analyzeTransactionRequest) {
        log.info("Inside Fallback processor for Trasaction Fraud Response");
        /**
         * TODO: Implement stand-in processing for Card Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return AnalyzeTransactionResponse.newBuilder().build();
    }



    /**
     * Fallback method for retrieving member details
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param memberQuery the MemberQuery object containing the necessary data for retrieving the member details based on memberId
     * @return a default MemberResponse instance as a fallback.
     */
    public MemberResponse fallbackProcessMemberServiceResponse(MemberQuery memberQuery) {
        log.info("Inside Fallback processor for Member Response");
        /**
         * TODO: Implement stand-in processing for Card Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return MemberResponse.newBuilder().build();
    }


    /**
     * Fallback method for retrieving member account details
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param accountQuery the AccountQuery object containing the necessary data for retrieving the member account details based on memberId
     * @return a default AccountDetailsResponse instance as a fallback.
     */
    public AccountDetailsResponse fallbackProcessAccountServiceResponse(AccountQuery accountQuery) {
        log.info("Inside Fallback processor for Member Response");
        /**
         * TODO: Implement stand-in processing for Card Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return AccountDetailsResponse.newBuilder().build();
    }


    /**
     * Fallback method for post ledger service
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param postTransactionRequest the PostTransactionRequest object containing the necessary data for posting the transaction in the ledger
     * @return a default PostTransactionResponse instance as a fallback.
     */
    public PostTransactionResponse fallbackProcessLedgerServiceResponse(PostTransactionRequest postTransactionRequest) {
        log.info("Inside Fallback processor for Ledger Response");
        /**
         * TODO: Implement stand-in processing for Ledger Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return PostTransactionResponse.newBuilder().build();
    }



    /**
     * Fallback method for BAS analysis response
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param analyzeBasket the AnalyzeBasket object containing the necessary data for Basket analysis
     * @return a default AnalyzeBasketResponse instance as a fallback.
     */
    public AnalyzeBasketResponse fallbackProcessBASServiceResponse(AnalyzeBasket analyzeBasket) {
        log.info("Inside Fallback processor for BAS service Response");
        /**
         * TODO: Implement stand-in processing for BAS Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return AnalyzeBasketResponse.newBuilder().build();
    }

    /**
     * Fallback method for Fraud service save transaction response
     * This method is invoked when the primary method fails and the Circuit Breaker is open.
     *
     * @param analyzeTransactionRequest the AnalyzeTransactionRequest object containing the necessary data for saving the transaction details to be
     * used for velocity check
     * @return a default SaveTransactionForVelocityCheckResponse instance as a fallback.
     */
    public SaveTransactionForVelocityCheckResponse fallbackProcessFraudSericeSaveTxnResponse(AnalyzeTransactionRequest analyzeTransactionRequest) {
        log.info("Inside Fallback processor for Fraud service velocity check Response");
        /**
         * TODO: Implement stand-in processing for Fraud service - Save Transaction for velocity check Service response.
         * This may include returning a predefined default response,
         * logging additional information, notifying other services,
         * or performing any other necessary fallback logic.
         */
        return SaveTransactionForVelocityCheckResponse.newBuilder().build();
    }

}
