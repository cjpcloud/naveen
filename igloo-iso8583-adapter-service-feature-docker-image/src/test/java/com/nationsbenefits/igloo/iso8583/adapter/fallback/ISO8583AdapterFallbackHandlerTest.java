package com.nationsbenefits.igloo.iso8583.adapter.fallback;
import com.nationsbenefits.igloo.authengine.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * <h1>ISO8583AdapterFallbackHandlerTest</h1>
 * This ISO8583AdapterFallbackHandlerTest is a junit test class for ISO8583AdapterFallbackHandler
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISO8583AdapterFallbackHandlerTest {


    private ISO8583AdapterFallbackHandler fallbackHandler;

    @BeforeEach
    public void setUp() {
        fallbackHandler = new ISO8583AdapterFallbackHandler();
    }

    @Test
    void testFallbackProcessAuthEngineResponse() {
        // Arrange
        AuthRequest authRequestMock = mock(AuthRequest.class);

        when(authRequestMock.getIsoMessage()).thenReturn(ISOMessage.newBuilder().build());

        // Act
        AuthResponse response = fallbackHandler.fallbackProcessAuthEngineResponse(authRequestMock);

        // Assert
        assertNotNull(response);
        assertEquals(AuthResponseCode.AUTH_ERROR.toString(), response.getStatusCode());
        assertNotNull(response.getResponseCode());
        assertEquals(AuthResponseCode.AUTH_ERROR, response.getResponseCode().getAuthResponseCode());
        assertEquals(AuthResponseCode.AUTH_ERROR.toString(), response.getResponseCode().getResponseDescription());
    }
}