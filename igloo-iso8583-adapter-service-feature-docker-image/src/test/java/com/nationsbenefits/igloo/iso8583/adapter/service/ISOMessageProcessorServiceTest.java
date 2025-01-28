package com.nationsbenefits.igloo.iso8583.adapter.service;

import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;

import com.nationsbenefits.igloo.event.publisher.service.EventPublisherService;
import com.nationsbenefits.igloo.iso8583.adapter.service.impl.ISOMessageProcessorServiceImpl;
import org.jpos.iso.ISOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h1>ISOMessageProcessorServiceTest</h1>
 * This ISOMessageProcessorServiceTest is a junit test class for ISOMessageProcessorServiceImpl
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISOMessageProcessorServiceTest {

    @InjectMocks
    private ISOMessageProcessorServiceImpl isoMessageProcessorService;

    @Mock
    private ISO8583AdapterService ISO8583AdapterService;

    @Mock
    private EventPublisherService eventPublisherService;

    private String authRequest = "01007238400108C1800116531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf41234567812345678901234500624=123USD";

    @BeforeEach
    public void setUp() {
        Map<String, Integer> currencyCodeMap = new HashMap<>();
        currencyCodeMap.put("USD", 840);
        ReflectionTestUtils.setField(isoMessageProcessorService, "currencyCodeMap", currencyCodeMap);
    }

    /**
     * Tests processing an ISO message with a successful authorization response.
     */
   @Test
    void testProcessISOMessage_success(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_ALLOW").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4001234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message with an invalid merchant authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_INVALID_MERCHANT(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_INVALID_MERCHANT").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4031234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message with an insufficient funds authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_INSUFFICIENT_FUNDS_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_INSUFFICIENT_FUNDS").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4511234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    @Test
    void testProcessISOMessage_AUTH_EXCEEDED_TRANSACTION_LIMIT_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_EXCEEDED_TRANSACTION_LIMIT").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4611234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message with an exceeded transaction limit authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_ACCOUNT_NOT_FOUND_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_ACCOUNT_NOT_FOUND").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4121234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message with auth partial allow authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_PARTIAL_ALLOW_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_PARTIAL_ALLOW").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4001234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card number is invalid authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_NUMBER_INVALID(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_NUMBER_INVALID").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4141234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card is inactive authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_NOT_ACTIVE(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_NOT_ACTIVATED").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4781234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card is expired authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_EXPIRED(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_EXPIRED").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4541234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card status is locked authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_LOCKED(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_LOCKED").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4381234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card type is not DEBIT authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_AUTHENTICATION_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_AUTHENTICATION_FAILED").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4821234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card CVV does not match authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CVV_MISMATCH(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CVV_MISMATCH").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4821234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card Pin is incorrect authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_PIN_VALIDATION_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_PIN_VALIDATION_FAILURE").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4551234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message when card validation is successful authorization response.
     */
    @Test
    void testProcessISOMessage_AUTH_CARD_SUCCESS(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("AUTH_CARD_SUCCESS").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4001234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
     * Tests processing an ISO message with a default failure response.
     */
    @Test
    void testProcessISOMessage_default_failure(){
        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("DEFAULT").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage(authRequest);
        assertNotNull(actualResponse);
        assertEquals("0110723840010AC1800016531445985901234400000000000000010001107221801234561234560007225912153003452177470003576d1a7a8f1ae806c79455a4ca9a322ba4bce9cb3b4e69f35f59649c3e8bcf4121234567812345678901234500624=123USD", actualResponse);
        verify(ISO8583AdapterService,times(1)).performAuthorization(any());
    }

    /**
    * Tests processing an ISO message with a failure in the JPOS parsing process.
    */
    @Test
    void testProcessISOMessage_jpos_failure() throws ISOException {

        AuthResponse authResponse = AuthResponse.newBuilder().setStatusCode("DEFAULT").build();
        when(ISO8583AdapterService.performAuthorization(any(AuthRequest.class))).thenReturn(authResponse);
        String actualResponse = isoMessageProcessorService.processISOMessage("tesr");
        assertEquals("", actualResponse);
    }

}
