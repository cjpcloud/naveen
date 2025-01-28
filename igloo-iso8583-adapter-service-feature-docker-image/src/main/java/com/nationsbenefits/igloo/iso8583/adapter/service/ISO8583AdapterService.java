package com.nationsbenefits.igloo.iso8583.adapter.service;

import com.nationsbenefits.igloo.authengine.grpc.AuthResponse;
import com.nationsbenefits.igloo.authengine.grpc.AuthRequest;

/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the interface Transaction Service.
 */
public interface ISO8583AdapterService {

    /**
     * This method will perform the Authorization on provided AuthRequest
     * @param authorizationRequest
     * @return
     */
    AuthResponse performAuthorization(AuthRequest authorizationRequest);

}
