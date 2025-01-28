package com.nationsbenefits.igloo.iso8583.adapter.service.impl;

import com.nationsbenefits.igloo.iso8583.adapter.client.ISO8583AdapterClientDelegate;
import com.nationsbenefits.igloo.iso8583.adapter.service.ISO8583AdapterService;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;
import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the Transaction Service layer Class to perform basic operations to initiate call to auth engine service for authorization.
 */
@Service
@Slf4j
public class ISO8583AdapterServiceImpl implements ISO8583AdapterService {

    @Autowired
    private ISO8583AdapterClientDelegate ISO8583AdapterClientDelegate;

    /**
     * This method will perform the transaction authorization and will call the auth engine through gRcp.
     * @param authorizationTxnRequest
     * @return
     */
    @Override
    public AuthResponse performAuthorization(AuthRequest authorizationTxnRequest) {
        return ISO8583AdapterClientDelegate.processAuthTransaction(authorizationTxnRequest);
    }


}
