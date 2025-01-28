package com.nationsbenefits.igloo.iso8583.adapter.controller;

import com.nationsbenefits.igloo.iso8583.adapter.api.Iso8583Api;
import com.nationsbenefits.igloo.iso8583.adapter.service.ISOMessageProcessorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 *
 * This class holds the REST endpoints for receiving ISO8583 message and process by its Message type indicator
 * This class is just to show demo as ISO8583 adapter service is a TCP/IP socket listener, once real implementation
 * and testing is possible this service will be removed.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "ISO8583 Adapter APIs", description = "This REST endpoints for receiving ISO8583 message and process by its Message type indicator")
public class ISO8583ApiImpl implements Iso8583Api {

    @Autowired
    ISOMessageProcessorService isoMessageProcessorService;

    /**
     * Method to consume ISO8583 message in byte array through /message REST end point and perform
     * parsing to make canonical data model to invoke auth engine service through gRPC.
     * @param msg
     */
    @Override
    public ResponseEntity<String> processISOMessage(String msg) {
        log.info("ISO8583 message : {} received by ISO8583-Adapter Controller ",msg) ;
        String responseMessage = isoMessageProcessorService.processISOMessage(msg);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }



}
