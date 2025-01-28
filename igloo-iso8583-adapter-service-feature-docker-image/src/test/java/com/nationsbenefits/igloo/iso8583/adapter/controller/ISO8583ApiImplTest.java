package com.nationsbenefits.igloo.iso8583.adapter.controller;

import com.nationsbenefits.igloo.iso8583.adapter.service.ISOMessageProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <h1>ISO8583ApiImplTest</h1>
 * This ISO8583ApiImplTest is a junit test class for ISO8583API
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 * @version 1.0.0
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class ISO8583ApiImplTest {

    @InjectMocks
    private ISO8583ApiImpl iso8583Api;
    @Mock
    private ISOMessageProcessorService isoMessageProcessorService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(iso8583Api).build();
    }


    @Test
    void testProcessISOMessage() throws Exception {
        // Given
        when(isoMessageProcessorService.processISOMessage(anyString())).thenReturn("01007238400108C08000165314459859012344000000000000000100011072218012345611345628102459100012345612345678909912345678123456789012345USD");

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/iso8583/message")
                .content("01007238400108C08000165314459859012344000000000000000100011072218012345611345628102459120612345612345678909912345678123456789012345USD")
            .contentType(MediaType.TEXT_PLAIN)).andExpect(status().isOk()).andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertEquals("01007238400108C08000165314459859012344000000000000000100011072218012345611345628102459100012345612345678909912345678123456789012345USD", responseContent);
        assertNotNull(responseContent);
        verify(isoMessageProcessorService, times(1)).processISOMessage(anyString());
    }

}
