package com.nationsbenefits.igloo.iso8583.adapter.service;

import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.ISOFormat;
import com.nationsbenefits.igloo.authengine.grpc.ISOMessage;
import com.nationsbenefits.igloo.iso8583.adapter.client.ISO8583AdapterClientDelegate;
import com.nationsbenefits.igloo.iso8583.adapter.service.impl.ISO8583AdapterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h1>ISO8583AdapterServiceTest</h1>
 * This ISO8583AdapterServiceTest is a junit test class for ISO8583AdapterServiceImpl
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISO8583AdapterServiceTest {

    @InjectMocks
    private ISO8583AdapterServiceImpl transactionService;

    @Mock
    private ISO8583AdapterClientDelegate ISO8583AdapterClientDelegate;

    private AuthRequest authRequest;

    private AuthResponse authResponse;

    @BeforeEach
    public void setUp(){
        authRequest = AuthRequest.newBuilder().setIsoMessage(ISOMessage.newBuilder().setIsoFormat(ISOFormat.newBuilder().setIsoFormatId("ISO8583").build()).build()).build();
        authResponse = AuthResponse.newBuilder().setStatusCode("ALLOW").build();
    }

    @Test
    void testPerformAuthorization() {
        when(ISO8583AdapterClientDelegate.processAuthTransaction(any())).thenReturn(authResponse);
        AuthResponse actualResponse = transactionService.performAuthorization(authRequest);
        verify(ISO8583AdapterClientDelegate, times(1)).processAuthTransaction(authRequest);
        assertEquals("ALLOW", actualResponse.getStatusCode());
    }




}
