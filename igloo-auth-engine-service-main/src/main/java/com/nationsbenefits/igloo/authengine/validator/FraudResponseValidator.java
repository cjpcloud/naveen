package com.nationsbenefits.igloo.authengine.validator;

import com.nationsbenefits.igloo.authengine.grpc.AdjudicationResponse;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationStatus;
import com.nationsbenefits.igloo.authengine.mapper.AuthEngineServiceMapper;
import com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.AnalyzeTransactionResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.FRAUD_SUSPECT_CODE;
import static com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants.FRAUD_SUSPECT_DESC;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.FRAUD_SUCCESS_LOG;
import static com.nationsbenefits.igloo.authengine.constant.LoggerConstants.POTENTIAL_FRAUD_LOG;
import static com.nationsbenefits.igloo.txn.fraud.analyzer.grpc.TxnFraudResponseCode.TXNFRAUD_ALLOW;

/**
 * Validator class for processing and validating fraud responses during adjudication.
 * This class contains logic to assess the fraud detection results from an external service
 * and determine whether a transaction can proceed or if it should be flagged for further review.
 * If no fraud is detected, the transaction is approved. If potential fraud is detected,
 * an adjudication response is sent, and the transaction is flagged as fraudulent.
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 * @author PwC
 */
@Slf4j
@Component
public class FraudResponseValidator {

    /**
     * Validates the fraud response during adjudication.
     * <p>
     * This method examines the fraud detection response and determines if the transaction is
     * safe to proceed or if it needs to be flagged as fraudulent. If fraud is detected, an
     * adjudication response is generated and sent via the provided {@code responseObserver}.
     * <p>
     * Specific scenarios handled:
     * <ul>
     *     <li>If the fraud response is {@code null}, validation is assumed to pass.</li>
     *     <li>If no fraud is detected (code matches {@code TXNFRAUD_ALLOW}), the method logs the result and allows the transaction.</li>
     *     <li>If potential fraud is detected, an adjudication response is generated, and the transaction is flagged.</li>
     * </ul>
     *
     * @param fraudResponse   the fraud detection response object containing fraud assessment results.
     * @param responseObserver the observer used to send the adjudication response back to the client.
     * @return {@code true} if no fraud is detected or if the fraud response is {@code null};
     *         {@code false} if fraud is detected and the transaction is flagged.
     */
    public boolean validateFraudResponseForAdjudication(AnalyzeTransactionResponse fraudResponse, StreamObserver<AdjudicationResponse> responseObserver) {

        if (Objects.isNull(fraudResponse)) {
            return true; // Assuming validation passes if fraudResponse is null
        }

        if (fraudResponse.getCode() == TXNFRAUD_ALLOW) {
            if(log.isInfoEnabled()) {
                log.info(FRAUD_SUCCESS_LOG);
            }
            return true;
        } else {
            if(log.isInfoEnabled()) {
                log.info(POTENTIAL_FRAUD_LOG);
            }
            AdjudicationResponse response = AuthEngineServiceMapper.buildAdjudicationResponseForFraud(
                AdjudicationStatus.newBuilder()
                    .setCode(FRAUD_SUSPECT_CODE)
                    .setDesc(FRAUD_SUSPECT_DESC)
                    .build()
            );
            // trigger event here
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return false;
        }
    }
}
