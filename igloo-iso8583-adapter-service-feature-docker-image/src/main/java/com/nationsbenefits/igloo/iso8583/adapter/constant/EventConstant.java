package com.nationsbenefits.igloo.iso8583.adapter.constant;
/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the Constant Class to store all event names  to use in ISO8583 adapter service
 */
public  interface EventConstant {

    public static final String ISO8583_MESSAGE_RECEIVED = "ISO8583_MESSAGE_RECEIVED_SUCCESSFULLY";

    public static final String ISO8583_MESSAGE_VALIDATION_FAILED = "ISO8583_MESSAGE_VALIDATION_FAILED";

    public static final String CANONICAL_MODEL_CREATED = "CANONICAL_MODEL_CREATED";

    public static final String CANONICAL_MODEL_CREATION_FAILED = "CANONICAL_MODEL_CREATION_FAILED";
    public static final String AUTHORIZATION_PROCESS_TRIGGERED = "AUTHORIZATION_PROCESS_TRIGGERED";

    public static final String AUTHORIZATION_SUCCESS = "AUTHORIZATION_SUCCESS";

    public static final String AUTHORIZATION_FAILED = "AUTHORIZATION_FAILED";

    public static final String ISO8583_AUTH_SUCCESS_RESPONSE_CREATED = "ISO8583_AUTH_SUCCESS_RESPONSE_CREATED";

    public static final String ISO8583_AUTH_FAILURE_RESPONSE_CREATED = "ISO8583_AUTH_FAILURE_RESPONSE_CREATED";

    public static final String ISO8583_AUTH_RESPONSE_SEND = "ISO8583_AUTH_RESPONSE_SEND";

}

