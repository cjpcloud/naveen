package com.nationsbenefits.igloo.iso8583.adapter.exception;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.nationsbenefits.igloo.iso8583.adapter.constant.ErrorConstant.*;
import static org.mockito.Mockito.*;
/**
 * <h1>ISOAdapterExceptionHandlerMutationTest</h1>
 * This ISOAdapterExceptionHandlerMutationTest is a junit test class for ISOAdapterExceptionHandler
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISOAdapterExceptionHandlerMutationTest {

    private ISOAdapterExceptionHandler isoAdapterExceptionHandler;
    private Map<String, Consumer<StatusRuntimeException>> errorHandlers;
    private Consumer<StatusRuntimeException> mockConsumer;

    @BeforeEach
    void setUp() {
        mockConsumer = mock(Consumer.class);
        errorHandlers = new HashMap<>();
        errorHandlers.put(INTERNAL,mockConsumer);

        isoAdapterExceptionHandler = new ISOAdapterExceptionHandler(errorHandlers);

    }

    @Test
    void testHandleRuntimeException_callsConsumerAccept() {
        StatusRuntimeException exception = mock(StatusRuntimeException.class);
        Status status = mock(Status.class);

        when(exception.getStatus()).thenReturn(status);
        when(status.getCode()).thenReturn(Status.Code.INTERNAL);

        isoAdapterExceptionHandler.handleRuntimeException(exception);

        verify(mockConsumer).accept(exception);
    }

    @Test
    void testHandleRuntimeException_unhandledErrorCode() {
        StatusRuntimeException exception = mock(StatusRuntimeException.class);
        Status status = mock(Status.class);

        when(exception.getStatus()).thenReturn(status);
        when(status.getCode()).thenReturn(Status.Code.UNKNOWN);

        isoAdapterExceptionHandler.handleRuntimeException(exception);

        verify(mockConsumer, never()).accept(any());
    }
}