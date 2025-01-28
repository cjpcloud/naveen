/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine.constant;

/**
 * Class to hold all the constant values to be used for logging in the Auth Engine Service.
 * This class contains various constants that are used for structured logging messages
 * throughout the Auth Engine Service.
 *
 * <p>These constants include log messages for different stages of the authorization process,
 * such as entry logs, completion logs, and exception logs. Using constants for log messages
 * helps in maintaining consistency and makes it easier to manage and update log messages
 * across the service.</p>
 *
 * <p>The class is designed to be non-instantiable by having a private constructor,
 * ensuring that it only serves as a container for log message constants.</p>
 *
 * @author PwC
 */
public interface LoggerConstants {

     /**
     * Constant representing the log message for authorization service entry.
     * This message is logged when an authorization request is received.
     */
    public static final String AUTH_SERVICE_ENTRY_LOG = "Authorization Request Received for transaction : {}";
    public static final String ADJ_SERVICE_ENTRY_LOG = "Adjudication Request Received for transaction : {}";

    /**
     * Constant representing the log message when card service response is retrieved.
     * This message is logged when the card service completes its response.
     */
    public static final String AUTH_SERVICE_CARD_DETAILS_COMPLETED = "Card Service Response Retrieved ..";

    /**
     * Constant representing the log message for an interrupt exception during processing.
     * This message is logged when an interrupt occurs while processing in the Auth Engine.
     */
    public static final String AUTH_SERVICE_INTERRUPT_EXCEPTION = "Interrupt: Error while processing - Auth Engine :{} ";

    /**
     * Constant representing the log message for an execution exception during processing.
     * This message is logged when an execution exception occurs while processing in the Auth Engine.
     */
    public static final String AUTH_SERVICE_EXECUTION_EXCEPTION = "Execution: Error while processing - Auth Engine :{} ";

    /**
     * Constant representing the log message for a general exception during processing.
     * This message is logged when any error occurs while processing in the Auth Engine.
     */
    public static final String AUTH_SERVICE_EXCEPTION = "Error while processing - Auth Engine :{} ";

    public static final String AUTH_SERVICE_STATUS_RUNTIME_EXCEPTION = "StatusRuntime Exception - Auth Engine :{} ";

    public static final String FRAUD_SUCCESS_LOG ="No Fraud Detected";

    public static final String POTENTIAL_FRAUD_LOG ="Potential Fraud Detected";

    public static final String CARD_SERVICE_INVOKE_LOG = "Invoking Card Service ..";

}
