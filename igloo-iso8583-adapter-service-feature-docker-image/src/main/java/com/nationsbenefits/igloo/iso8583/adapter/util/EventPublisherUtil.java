package com.nationsbenefits.igloo.iso8583.adapter.util;

import com.google.protobuf.Timestamp;
import com.nationsbenefits.igloo.authengine.grpc.*;
import com.nationsbenefits.igloo.common.models.grpc.Transaction;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.domain.event.EventPayload;
import com.nationsbenefits.igloo.iso8583.adapter.exception.ISOAdapterCircuitBreakerFallbackException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
/**
 *
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * This is Utility class for building event headers and payloads for event publishing.
 * This class is particularly useful in the context of processing ISO8583 messages, handling authorization transactions,
 * and integrating with the Auth Engine and other services. It aids in the creation of standardized event data structures
 * for consistent logging and event handling across the application.
 */

public class EventPublisherUtil {

    /**
     * Processes an authorization transaction by sending a gRPC request to the Auth Engine.
     * The method integrates Circuit Breaker and Retry mechanisms to handle potential failures gracefully.
     *
     * @param authorizationRequest the authorization request object containing the necessary data for authorization.
     * @return the authorization response object received from the Auth Engine.
     * @throws ISOAdapterCircuitBreakerFallbackException if the authorization request fails after all retries.
     */

    public static EventHeader buildEventHeader(AuthRequest authorizationRequest,String txnType, String appName, String version) {

        Transaction transaction=authorizationRequest.getIsoMessage().getTransaction();
        EventHeader eventHeader=new EventHeader();
        eventHeader.setSourceApplicationName(appName);
        eventHeader.setSourceVersion(version);
        eventHeader.setTransactionId(transaction.getID());
        eventHeader.setBasTransactionId(transaction.getNationsBenefitsGeneratedId());
        eventHeader.setTxnLocDateTime(convertTimeStamp(transaction.getLocalTime()));
        eventHeader.setTxnLocation(transaction.getLocation().getCountry()); //field 19
        eventHeader.setTxnAmount(String.valueOf(transaction.getAmount().getAmount()));
        eventHeader.setTxnCurrency(transaction.getAmount().getSCurrencyCode());
        eventHeader.setTxnType(txnType);
        eventHeader.setTxnMsgType(authorizationRequest.getIsoMessage().getMessageType().getMessageType());
        eventHeader.setPanHASH(authorizationRequest.getIsoMessage().getCard().getPanHash());
        eventHeader.setMccCode(authorizationRequest.getIsoMessage().getMerchant().getCategoryCode());
        eventHeader.setCreatedTimestamp(LocalDateTime.now());
        return eventHeader;
    }

    /**
     * Builds an {@code EventHeader} object with the specified transaction ID.
     * <p>
     * This method creates a new {@code EventHeader}, sets its transaction ID,
     * and assigns the current local date and time as the created timestamp.
     * </p>
     *
     * @param transactionId the transaction ID to be set in the event header
     * @return a new {@code EventHeader} object with the provided transaction ID and current timestamp
     */
    public static EventHeader buildEventHeader(String transactionId) {

        EventHeader eventHeader=new EventHeader();
        eventHeader.setTransactionId(transactionId);
        eventHeader.setCreatedTimestamp(LocalDateTime.now());
        return eventHeader;
    }

    /**
     * Builds an {@code EventPayload} object with the specified event name and payload string.
     * <p>
     * This method creates a new {@code EventPayload}, sets its event name,
     * event description (using the event name), current date and time, and the extended data payload.
     * </p>
     *
     * @param eventName  the name of the event
     * @param payloadStr the payload string containing extended data
     * @return a new {@code EventPayload} object with the provided details
     */
    public static EventPayload buildEventPayload(String eventName,String payloadStr) {
        EventPayload eventPayload=new EventPayload();
        eventPayload.setEventName(eventName);
        eventPayload.setEventDescription(eventName);
        eventPayload.setEventDateTime(LocalDateTime.now());
        eventPayload.setExtendedDataPayload(payloadStr);
        return eventPayload;
    }


    /**
     * Converts a {@code Timestamp} object to a {@code LocalDateTime} object at the start of the day.
     * <p>
     * If the provided {@code Timestamp} is not null, this method converts it to
     * an {@code Instant}, adjusts it to the system's default time zone, and returns
     * the corresponding {@code LocalDateTime} at the start of the day.
     * If the {@code Timestamp} is null, it returns {@code null}.
     * </p>
     *
     * @param timestamp the {@code Timestamp} to be converted
     * @return a {@code LocalDateTime} representing the same point in time at the start of the day,
     *         or {@code null} if the input is null
     */
    private static LocalDateTime convertTimeStamp(Timestamp timestamp){

        if(Objects.nonNull(timestamp)) {
            return Instant
                    .ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().atStartOfDay();
        }else{
            return null;
        }
    }
}
