package com.nationsbenefits.igloo.iso8583.adapter.constant;
/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the Constant Class to store all constants to use in ISO8583 adapter service
 */
public  interface ISOAdapterConstant {

    public static final String ISO_MESSAGE_FORMAT_8583 = "ISO8583";
    public static final String MESSAGE_TYPE_AUTHORIZATION = "AUTHORIZATION";
    public static final String CHANNEL_MASTERCARD = "MASTERCARD";
    public static final String MESSAGE_TEMPLATE_ISO8583 =  "/fields.xml";
    public static final String SERVER_PORT_9091 = "localhost:9093";
    public static final String LOCATION_US = "US";
    public static final String MTI_AUTH_REQUEST = "0100";
    public static final String MTI_AUTH_RESPONSE = "0110";
    public static final String ISO_8583_SUCCESS = "00";
    public static final String ISO_8583_INVALID_MERCHANT = "03";
    public static final String ISO_8583_INSUFFICIENT_FUND = "51";
    public static final String ISO_8583_EXCEEDED_TRANSACTION_LIMIT = "61";
    public static final String ISO_8583_TXN_INVALID= "12";

    public static final String ISO_8583_CARD_NUMBER_INVALID= "14";
    public static final String ISO_8583_CARD_NOT_ACTIVATED= "78";
    public static final String ISO_8583_CARD_EXPIRED= "54";
    public static final String ISO_8583_CARD_LOCKED= "38";
    public static final String ISO_8583_CARD_AUTHENTICATION_FAILED= "82";
    public static final String ISO_8583_CVV_MISMATCH= "82";
    public static final String ISO_8583_PIN_VALIDATION_FAILURE= "55";
    public static final String ISO_8583_CARD_SUCCESS= "00";
    public static final String ISO_8583_PARTIAL_APPROVAL= "10";
    public static final String RESPONSE_CODE_SUCCESS = "ALLOW";

    public static final String MD5 ="MD5";
    /**
     * The constant SWAGGER_TITLE.
     */
    public static final String SWAGGER_TITLE = "Igloo ISO8583 Adapter Service";
    /**
     * The constant SWAGGER_VERSION.
     */
    public static final String SWAGGER_VERSION = "1.0.0";
    /**
     * The constant SWAGGER_DESCRIPTION.
     */
    public static final String SWAGGER_DESCRIPTION = "Igloo ISO8583 Adapter Service";

    public static final String AUTH_ALLOW_SUCCESS="AUTH_ALLOW";
    public static final String AUTH_INVALID_MERCHANT="AUTH_INVALID_MERCHANT";
    public static final String AUTH_INSUFFICIENT_FUNDS="AUTH_INSUFFICIENT_FUNDS";
    public static final String AUTH_EXCEEDED_TRANSACTION_LIMIT="AUTH_EXCEEDED_TRANSACTION_LIMIT";
    public static final String AUTH_PARTIAL_ALLOW="AUTH_PARTIAL_ALLOW";
    public static final String AUTH_ACCOUNT_NOT_FOUND="AUTH_ACCOUNT_NOT_FOUND";
    public static final String AUTH_CARD_NUMBER_INVALID = "AUTH_CARD_NUMBER_INVALID";
    public static final String AUTH_CARD_NOT_ACTIVATED = "AUTH_CARD_NOT_ACTIVATED";
    public static final String AUTH_CARD_EXPIRED = "AUTH_CARD_EXPIRED";
    public static final String AUTH_CVV_MISMATCH = "AUTH_CVV_MISMATCH";
    public static final String AUTH_CARD_AUTHENTICATION_FAILED = "AUTH_CARD_AUTHENTICATION_FAILED";
    public static final String AUTH_CARD_SUCCESS = "AUTH_CARD_SUCCESS";
    public static final String AUTH_PIN_VALIDATION_FAILURE = "AUTH_PIN_VALIDATION_FAILURE";
    public static final String AUTH_CARD_LOCKED = "AUTH_CARD_LOCKED";


}
