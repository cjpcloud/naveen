package com.nationsbenefits.igloo.iso8583.adapter.service;

/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the interface to perform basic operations like parsing messages , validating and initiate call to authe engine service
 */
public interface ISOMessageProcessorService {

    String  processISOMessage(String isoMsg);


}
