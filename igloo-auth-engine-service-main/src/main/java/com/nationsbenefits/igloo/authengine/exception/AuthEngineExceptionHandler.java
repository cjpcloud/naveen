package com.nationsbenefits.igloo.authengine.exception;


import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.nationsbenefits.igloo.authengine.constant.ErrorConstant.*;


/**
 *
 *  @author PwC
 *  Copyright Â© 2024, NationsBenefits. All Rights reserved
 *
 * The AuthEngineExceptionHandler class provides methods to handle various gRPC status runtime exceptions.
 * Each method is responsible for handling a specific error code and logging the handling information.
 */
@Slf4j
@Component
public class AuthEngineExceptionHandler {

    private final Map<String, Consumer<StatusRuntimeException>> errorHandlers;

    /**
     * Constructs an ISOAdapterExceptionHandler and initializes the error handlers.
     */
    public AuthEngineExceptionHandler() {
        errorHandlers = new HashMap<>();
        errorHandlers.put(INTERNAL, this::handleInternalErrorCode);
        errorHandlers.put(UNAVAILABLE, this::handleUnavailableErrorCode);
        errorHandlers.put(DEADLINE_EXCEEDED, this::handleDeadlineExceededErrorCode);
        errorHandlers.put(NOT_FOUND, this::handleNotFoundErrorCode);
        errorHandlers.put(PERMISSION_DENIED, this::handlePermissionDeniedErrorCode);
        errorHandlers.put(RESOURCE_EXHAUSTED, this::handleResourceExhaustedErrorCode);
        errorHandlers.put(UNIMPLEMENTED, this::handleUnimplementedErrorCode);
        errorHandlers.put(DATA_LOSS, this::handleDataLossErrorCode);
    }

    /**
     * Handles the RuntimeException based on the error code.
     * Routes the exception to the appropriate handler method.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    public void handleRuntimeException(StatusRuntimeException statusRuntimeException) {
        String errorCode = statusRuntimeException.getStatus().getCode().toString();
        Consumer<StatusRuntimeException> handler = errorHandlers.get(errorCode);
        if (handler != null) {
            handler.accept(statusRuntimeException);
        } else {
            log.info("StatusRuntimeException : Unhandled Error Code : " + errorCode);
        }
    }

    /**
     * Handles the INTERNAL error code.
     *
     * TODO: Implement specific error handling logic for INTERNAL error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleInternalErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : INTERNAL Handler");
    }

    /**
     * Handles the UNAVAILABLE error code.
     *
     * TODO: Implement specific error handling logic for UNAVAILABLE error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleUnavailableErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : UNAVAILABLE Handler");
    }

    /**
     * Handles the DEADLINE_EXCEEDED error code.
     *
     * TODO: Implement specific error handling logic for DEADLINE_EXCEEDED error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleDeadlineExceededErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : DEADLINE_EXCEEDED Handler");
    }

    /**
     * Handles the NOT_FOUND error code.
     *
     * TODO: Implement specific error handling logic for NOT_FOUND error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleNotFoundErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : NOT_FOUND Handler");
    }

    /**
     * Handles the PERMISSION_DENIED error code.
     *
     * TODO: Implement specific error handling logic for PERMISSION_DENIED error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handlePermissionDeniedErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : PERMISSION_DENIED Handler");
    }

    /**
     * Handles the RESOURCE_EXHAUSTED error code.
     *
     * TODO: Implement specific error handling logic for RESOURCE_EXHAUSTED error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleResourceExhaustedErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : RESOURCE_EXHAUSTED Handler");
    }

    /**
     * Handles the UNIMPLEMENTED error code.
     *
     * TODO: Implement specific error handling logic for UNIMPLEMENTED error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleUnimplementedErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : UNIMPLEMENTED Handler");
    }

    /**
     * Handles the DATA_LOSS error code.
     *
     * TODO: Implement specific error handling logic for DATA_LOSS error code.
     *
     * @param statusRuntimeException the StatusRuntimeException to handle
     */
    private void handleDataLossErrorCode(StatusRuntimeException statusRuntimeException) {
        log.info("StatusRuntimeException : Error Code : DATA_LOSS Handler");
    }
}
