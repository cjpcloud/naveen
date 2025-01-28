package com.nationsbenefits.igloo.iso8583.adapter.exception;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;
/**
 * <h1>ISOAdapterExceptionHandlerTest</h1>
 * This ISOAdapterExceptionHandlerTest is a junit test class for ISOAdapterExceptionHandler
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISOAdapterExceptionHandlerTest {

    private ISOAdapterExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ISOAdapterExceptionHandler();
    }

    @Test
    void testHandleInternalErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INTERNAL);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);

    }

    @Test
    void testHandleUnavailableErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNAVAILABLE);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleDeadlineExceededErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.DEADLINE_EXCEEDED);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleNotFoundErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.NOT_FOUND);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandlePermissionDeniedErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.PERMISSION_DENIED);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleResourceExhaustedErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.RESOURCE_EXHAUSTED);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleUnimplementedErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNIMPLEMENTED);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleDataLossErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.DATA_LOSS);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void testHandleUnhandledErrorCode() {
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNKNOWN);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }

    @Test
    void test_handleRuntimeException_null_handler() {

        StatusRuntimeException exception = new StatusRuntimeException(Status.OK);
        ISOAdapterExceptionHandler spyHandler = spy(exceptionHandler);

        spyHandler.handleRuntimeException(exception);


    }


}