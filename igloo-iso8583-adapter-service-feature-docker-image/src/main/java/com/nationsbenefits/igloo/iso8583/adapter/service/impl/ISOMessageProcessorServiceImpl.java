package com.nationsbenefits.igloo.iso8583.adapter.service.impl;


import com.google.protobuf.Timestamp;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.common.models.grpc.*;
import com.nationsbenefits.igloo.authengine.grpc.Channel;
import com.nationsbenefits.igloo.authengine.grpc.ISOFormat;
import com.nationsbenefits.igloo.authengine.grpc.ISOMessage;
import com.nationsbenefits.igloo.authengine.grpc.MessageType;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import com.nationsbenefits.igloo.iso8583.adapter.constant.EventConstant;
import com.nationsbenefits.igloo.iso8583.adapter.service.ISOMessageProcessorService;
import com.nationsbenefits.igloo.iso8583.adapter.service.ISO8583AdapterService;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nationsbenefits.igloo.iso8583.adapter.constant.ISOAdapterConstant.*;
import static com.nationsbenefits.igloo.iso8583.adapter.util.EventPublisherUtil.buildEventHeader;
import static com.nationsbenefits.igloo.iso8583.adapter.util.EventPublisherUtil.buildEventPayload;

/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the Service layer Class to perform basic operations like parsing messages , validating and initiate call to authe engine service
 */
@Service
@Slf4j
public class ISOMessageProcessorServiceImpl implements ISOMessageProcessorService {

    @Autowired
    private ISO8583AdapterService iso8583AdapterService;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Value("#{${currencyCode}}")
    private Map<String, Integer> currencyCodeMap;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.application.version}")
    private String version;

    /**
     * Method to consume message and convert to ISO8583 message object
     * and initiate method call to create canonical data model and invoke auth engine
     * @param msg
     * @return
     */
    @Override
    public String processISOMessage(String msg) {

        InputStream inputStream = ISOMessageProcessorServiceImpl.class.getResourceAsStream(MESSAGE_TEMPLATE_ISO8583);
        GenericPackager packager;
        String transactionId=UUID.randomUUID().toString();
        AuthRequest authRequest = null;
        EventHeader eventHeader=null;
        try {
            packager = new GenericPackager(inputStream);
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener());
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            EventHeader eventHeaderWihTxnId=buildEventHeader(transactionId);
            try {
                isoMsg.unpack(msg.getBytes());
            } catch (ISOException e) {
                eventPublisherService.publishEventAsync(eventHeaderWihTxnId, List.of(buildEventPayload(EventConstant.ISO8583_MESSAGE_VALIDATION_FAILED,e.getMessage())));
                log.error("ISOException while processing ISO8583 messages in ISO8583 adapter service :{}",e.getMessage());
            }
            eventPublisherService.publishEventAsync(eventHeaderWihTxnId, List.of(buildEventPayload(EventConstant.ISO8583_MESSAGE_RECEIVED,msg)));
        try {
            authRequest = buildCanonicalDataModel(isoMsg,transactionId);
            eventPublisherService.publishEventAsync(buildEventHeader(authRequest,transactionId,appName,version), List.of(buildEventPayload(EventConstant.CANONICAL_MODEL_CREATED,authRequest.toString())));

        } catch (NoSuchAlgorithmException e) {
            eventPublisherService.publishEventAsync(eventHeaderWihTxnId, List.of(buildEventPayload(EventConstant.CANONICAL_MODEL_CREATION_FAILED,e.getMessage())));
            log.error("NoSuchAlgorithmException while processing ISO8583 messages in ISO8583 adapter service :{}",e.getMessage());
        }
        eventHeader= buildEventHeader(authRequest,MESSAGE_TYPE_AUTHORIZATION,appName,version);
        eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.AUTHORIZATION_PROCESS_TRIGGERED,authRequest.toString())));
        AuthResponse authResponse = iso8583AdapterService.performAuthorization(authRequest);
          if (null != authResponse) {
                try {
                    if (isoMsg.getMTI().equalsIgnoreCase(MTI_AUTH_REQUEST)) {
                        if(authResponse.getStatusCode().equalsIgnoreCase(AUTH_ALLOW_SUCCESS))
                        {
                            eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.AUTHORIZATION_SUCCESS,msg),buildEventPayload(EventConstant.ISO8583_AUTH_SUCCESS_RESPONSE_CREATED,authResponse.toString())));
                        }else{
                            eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.AUTHORIZATION_FAILED,msg),buildEventPayload(EventConstant.ISO8583_AUTH_FAILURE_RESPONSE_CREATED,authResponse.toString())));
                        }
                        buildAuthResponse(isoMsg,authResponse.getStatusCode());
                        eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.ISO8583_AUTH_RESPONSE_SEND,authRequest.toString())));
                        return new String(isoMsg.pack());
                    }
                } catch (ISOException e) {
                    eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.AUTHORIZATION_FAILED,e.getMessage())));
                    log.error("ISOException while processing ISO8583 response in ISO8583 adapter service :{}",e.getMessage());
                }

            }
        } catch (Exception ex) {
          eventPublisherService.publishEventAsync(eventHeader, List.of(buildEventPayload(EventConstant.AUTHORIZATION_FAILED, ex.getMessage())));
          log.error("Exception while processing ISO8583 messages in ISO8583 adapter service :{}",ex.getMessage());
        }
        return "";
    }


 
    /**
     * Method to build Canonical data model from ISO8583 message structure and pass to Auth Service to perform Transaction Authorization.
     * @param isoMsg
     * @return
     */
    private AuthRequest buildCanonicalDataModel(ISOMsg isoMsg,String transactionId) throws NoSuchAlgorithmException {

        log.info("Auth engine canonical data model construction started from iso8583 message") ;
        AuthRequest.Builder authTxnReqBuilder = AuthRequest.newBuilder().setIsoMessage(ISOMessage.newBuilder().setCard(
                        Card.newBuilder().setPanHash(
                                getMD5Hex(isoMsg.getString(2))).setCvv(getMD5Hex(getSubelement(isoMsg.getString(48), "24"))).build()).setTransaction(
                        Transaction.newBuilder()
                                .setAmount(Amount.newBuilder()
                                        .setAmount(Long.parseLong(isoMsg.getString(4)))
                                        .setSCurrencyCode(isoMsg.getString(49))
                                        .setCurrencyCode(extractCurrencyCode(isoMsg.getString(49))).build())
                                .setID(transactionId)
                                .setNationsBenefitsGeneratedId(isoMsg.getString(37))
                                .setLocalTime(Timestamp.newBuilder().setSeconds(Long.parseLong(isoMsg.getString(12))).build())
                                .setUtcTime(Timestamp.newBuilder().setSeconds(Long.parseLong(isoMsg.getString(7))).build())
                                .build()
                                 )
                .setIsoFormat(ISOFormat.newBuilder().setIsoFormatId(ISO_MESSAGE_FORMAT_8583).build())
                .setMessageType(MessageType.newBuilder().setMessageType(MESSAGE_TYPE_AUTHORIZATION).build())
                .setChannel(Channel.newBuilder().setChannel(CHANNEL_MASTERCARD).build())
                .setMerchant(Merchant.newBuilder().setCategoryCode(isoMsg.getString(18)).setID(isoMsg.getString(32)).build()));
        AuthRequest authorizationTxnRequest = authTxnReqBuilder.build();
        log.info("Auth engine canonical data model construction completed {}", authorizationTxnRequest) ;

        return authorizationTxnRequest;
    }

    /**
     * Sets the currency code based on the provided cuurency string.
     *
     * @param currencyString The currency string (e.g., "USD").
     * @return The corresponding numeric currency code (e.g., 840).
     */
    private Integer extractCurrencyCode(String currencyString) {
        Integer currencyCode = currencyCodeMap.get(currencyString);
        if (currencyCode == null) {
            throw new IllegalArgumentException("Provided invalid currency code: " + currencyString);
        }
        return currencyCode;
    }

    /**
     * Extracts the value of a specific subelement from the DE string.
     *
     * <p>This method parses the DE field, which may contain multiple subelements separated
     * by a delimiter (e.g., '|'). Each subelement is expected to be in the format
     * {@code subelementId=value}, and the method retrieves the value associated with the
     * specified subelement ID.
     *
     * @param dataElement  The DE string containing multiple subelements (e.g., "24=836|25=12345").
     * @param subelementId The ID of the subelement to retrieve (e.g., "24").
     * @return The value of the specified subelement (e.g., "836" for subelement "24"), or {@code null}
     *         if the subelement is not found or if the DE48 string is null/empty.
     */
    private static String getSubelement(String dataElement, String subelementId) {
        if (dataElement == null || dataElement.isEmpty()) {
            return null;
        }
        // Split DE by delimiter (assuming '=' or '|' is the delimiter for subelements)
        String[] subelements = dataElement.split("\\|"); // Adjust delimiter if necessary
        for (String subelement : subelements) {
            if (subelement.startsWith(subelementId + "=")) {
                return subelement.split("=")[1]; // Return the value of the subelement
            }
        }
        return null; // Return null if subelement is not found
    }

    /**
     * Method construct iso8583 success response
     * @param isoMsg
     * @throws ISOException
     */
    private void buildAuthResponse(ISOMsg isoMsg,String responseCode) throws ISOException {
        isoMsg.setMTI(MTI_AUTH_RESPONSE);
        assignResponseCodeToISO8583Message(isoMsg,responseCode);
    }

    /**
     * Method to get MD5 hash from string
     * @param inputString
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5Hex(final String inputString) throws NoSuchAlgorithmException {
        if (inputString == null || inputString.isEmpty()) {
            return null;
        }
        MessageDigest md = MessageDigest.getInstance(MD5);
        md.update(inputString.getBytes());
        byte[] digest = md.digest();
        return convertByteToHex(digest);
    }

    /**
     * Method to convert Byte to Hexa decimal format
     * @param byteData
     * @return
     */
    private static String convertByteToHex(byte[] byteData) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * Method to assign response code to ISO8583 adapter
     */
    private void assignResponseCodeToISO8583Message(ISOMsg isoMsg,String responseCode){

        switch (responseCode) {
            case AUTH_ALLOW_SUCCESS:
                log.info("ISO8583 adapter set 00(Successful approval from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_SUCCESS);
                break;
            case AUTH_INVALID_MERCHANT:
                log.info("ISO8583 adapter set 03(invalid merchant from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_INVALID_MERCHANT);
                break;
            case AUTH_INSUFFICIENT_FUNDS:
                log.info("ISO8583 adapter set 51(insufficient fund from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_INSUFFICIENT_FUND);
                break;
            case AUTH_EXCEEDED_TRANSACTION_LIMIT:
                log.info("ISO8583 adapter set 61(exceeded transaction limit from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_EXCEEDED_TRANSACTION_LIMIT);
                break;
            case AUTH_PARTIAL_ALLOW:
                log.info("ISO8583 adapter set 10(partial allowed transaction from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_SUCCESS);
                break;
            case AUTH_ACCOUNT_NOT_FOUND:
                log.info("ISO8583 adapter set 12(transaction invalid from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_TXN_INVALID);
                break;
            case AUTH_CARD_NUMBER_INVALID:
                log.info("ISO8583 adapter set 14(card number invalid from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_NUMBER_INVALID);
                break;
            case AUTH_CARD_NOT_ACTIVATED:
                log.info("ISO8583 adapter set 78(card is found but Status is not active amount from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_NOT_ACTIVATED);
                break;
            case AUTH_CARD_EXPIRED:
                log.info("ISO8583 adapter set 54(card is found but expired from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_EXPIRED);
                break;
            case AUTH_CARD_LOCKED:
                log.info("ISO8583 adapter set 38(Card status is locked from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_LOCKED);
                break;
            case AUTH_CARD_AUTHENTICATION_FAILED:
                log.info("ISO8583 adapter set 82(Card found and card type is NOT debit but card status is active from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_AUTHENTICATION_FAILED);
                break;
            case AUTH_CVV_MISMATCH:
                log.info("ISO8583 adapter set 82(card CVV did not match from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CVV_MISMATCH);
                break;
            case AUTH_PIN_VALIDATION_FAILURE:
                log.info("ISO8583 adapter set 55(Card pin did not match from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_PIN_VALIDATION_FAILURE);
                break;
            case AUTH_CARD_SUCCESS:
                log.info("ISO8583 adapter set 00(Card found, status is active and not expired from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_CARD_SUCCESS);
                break;
            default:
                log.info("ISO8583 adapter set 12(invalid transaction from Auth Engine) in 8583 auth response") ;
                isoMsg.set(39, ISO_8583_TXN_INVALID);
        }
    }
}
