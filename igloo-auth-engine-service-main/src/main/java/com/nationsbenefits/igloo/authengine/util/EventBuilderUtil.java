package com.nationsbenefits.igloo.authengine.util;

import com.google.protobuf.Timestamp;
import com.nationsbenefits.igloo.authengine.grpc.AdjudicationRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.common.models.grpc.Transaction;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.domain.event.EventPayload;

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
 * This class is particularly useful in the context of processing Adjudication/Auth  request, handling authorization transactions,
 * and integrating with the Auth Engine and other services. It aids in the creation of standardized event data structures
 * for consistent logging and event handling across the application.
 */

public class EventBuilderUtil {

    /**
     * Build Event Header from Adjudication Request
     *
     * @param request the adjudication request object containing the necessary data for adjudication.
     * @param appName the application name.
     * @return the Event Header.
     */

    public static EventHeader buildEventHeaderFromAdjudicationModel(AdjudicationRequest request,String appName,String version) {

        Transaction transaction=request.getTransaction();
        EventHeader eventHeader=new EventHeader();
        eventHeader.setSourceApplicationName(appName);
        eventHeader.setSourceVersion(version);
        eventHeader.setCreatedBy(appName);
        eventHeader.setTransactionId(transaction.getID());
        eventHeader.setBasTransactionId(transaction.getNationsBenefitsGeneratedId());
        eventHeader.setTxnLocDateTime(convertTimeStamp(transaction.getLocalTime()));
        eventHeader.setTxnLocation(transaction.getLocation().getCountry()); //field 19
        eventHeader.setTxnAmount(String.valueOf(transaction.getAmount().getAmount()));
        eventHeader.setTxnCurrency(transaction.getAmount().getSCurrencyCode());
        eventHeader.setPanHASH(request.getCard().getPanHash());
        eventHeader.setMccCode(request.getMerchant().getCategoryCode());
        eventHeader.setCreatedTimestamp(LocalDateTime.now());
        return eventHeader;
    }

    /**
     * Build Event Header from AuthRequest Request
     *
     * @param request the authRequest request object containing the necessary data for adjudication.
     * @param appName the application name.
     * @return the Event Header.
     */

    public static EventHeader buildEventHeaderFromAuthorizationModel(AuthRequest request, String appName, String version) {
        EventHeader eventHeader=new EventHeader();
        eventHeader.setSourceApplicationName(appName);
        eventHeader.setSourceVersion(version);
        eventHeader.setCreatedBy(appName);
        eventHeader.setTransactionId(request.getIsoMessage().getTransaction().getID());
        eventHeader.setBasTransactionId(request.getIsoMessage().getTransaction().getNationsBenefitsGeneratedId());
        eventHeader.setTxnLocDateTime(convertTimeStamp(request.getIsoMessage().getTransaction().getLocalTime()));
        eventHeader.setTxnLocation(request.getIsoMessage().getTransaction().getLocation().getCountry()); //field 19
        eventHeader.setTxnAmount(String.valueOf(request.getIsoMessage().getTransaction().getAmount().getAmount()));
        eventHeader.setTxnCurrency(request.getIsoMessage().getTransaction().getAmount().getSCurrencyCode());
        eventHeader.setTxnMsgType(request.getIsoMessage().getMessageType().getMessageType());
        eventHeader.setPanHASH(request.getIsoMessage().getCard().getPanHash());
        eventHeader.setMccCode(request.getIsoMessage().getMerchant().getCategoryCode());
        eventHeader.setCreatedTimestamp(LocalDateTime.now());
        return eventHeader;
    }

    /**
     * Adds the current timestamp to the provided {@code EventHeader}.
     * <p>
     * This method sets the {@code createdTimestamp} field of the given {@code EventHeader}
     * to the current local date and time.
     * </p>
     *
     * @param eventHeader the {@code EventHeader} object to which the timestamp will be added
     * @return the updated {@code EventHeader} with the {@code createdTimestamp} set
     */
    public static EventHeader addTimeStampToHeader(EventHeader eventHeader) {
        eventHeader.setCreatedTimestamp(LocalDateTime.now());
        return eventHeader;
    }

    /**
     * Builds an {@code EventPayload} object with the specified event name and payload string.
     * <p>
     * This method creates a new {@code EventPayload}, sets its event name, description,
     * current date and time, and extended data payload.
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
     * Converts a {@code Timestamp} object to a {@code LocalDateTime} object.
     * <p>
     * If the provided {@code Timestamp} is not null, this method converts it to
     * an {@code Instant}, adjusts it to the system's default time zone, and returns
     * the corresponding {@code LocalDateTime} at the start of the day.
     * If the {@code Timestamp} is null, it returns {@code null}.
     * </p>
     *
     * @param timestamp the {@code Timestamp} to be converted
     * @return a {@code LocalDateTime} object representing the same point in time,
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
