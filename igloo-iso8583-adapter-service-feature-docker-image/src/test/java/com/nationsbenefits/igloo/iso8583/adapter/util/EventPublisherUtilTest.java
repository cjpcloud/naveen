package com.nationsbenefits.igloo.iso8583.adapter.util;

import com.google.protobuf.Timestamp;
import com.nationsbenefits.igloo.common.models.grpc.*;
import com.nationsbenefits.igloo.domain.event.EventHeader;
import com.nationsbenefits.igloo.domain.event.EventPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
/**
 * <h1>EventPublisherUtilTest</h1>
 * This EventPublisherUtilTest is a junit test class for EventPublisherUtil
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @since 29th October 2024
 */
@ExtendWith(SpringExtension.class)
class EventPublisherUtilTest {

    @Test
    void testBuildEventHeaderWithAuthRequest() {
        com.nationsbenefits.igloo.authengine.grpc.AuthRequest authRequest = createTestAuthRequest();
        String txnType = "txnType";

        EventHeader eventHeader = EventPublisherUtil.buildEventHeader(authRequest, txnType,"appName","version");

        assertNotNull(eventHeader);
        assertEquals("txnId", eventHeader.getTransactionId());
        assertEquals("basTxnId", eventHeader.getBasTransactionId());
        assertEquals("country", eventHeader.getTxnLocation());
        assertEquals("1000", eventHeader.getTxnAmount());
        assertEquals("USD", eventHeader.getTxnCurrency());
        assertEquals(txnType, eventHeader.getTxnType());
        assertEquals("messageType", eventHeader.getTxnMsgType());
        assertEquals("panHash", eventHeader.getPanHASH());
        assertEquals("mccCode", eventHeader.getMccCode());
        assertNotNull(eventHeader.getCreatedTimestamp());
    }


    @Test
    void testBuildEventHeaderWithTransactionId() {
        String transactionId = "txnId";

        EventHeader eventHeader = EventPublisherUtil.buildEventHeader(transactionId);

        assertNotNull(eventHeader);
        assertEquals(transactionId, eventHeader.getTransactionId());
        assertNotNull(eventHeader.getCreatedTimestamp());
    }

    @Test
    void testBuildEventPayload() {
        String eventName = "eventName";
        String payloadStr = "payloadStr";

        EventPayload eventPayload = EventPublisherUtil.buildEventPayload(eventName, payloadStr);

        assertNotNull(eventPayload);
        assertEquals(eventName, eventPayload.getEventName());
        assertEquals(eventName, eventPayload.getEventDescription());
        assertEquals(payloadStr, eventPayload.getExtendedDataPayload());
        assertNotNull(eventPayload.getEventDateTime());
    }


    private com.nationsbenefits.igloo.authengine.grpc.AuthRequest createTestAuthRequest() {
        Transaction transaction = Transaction.newBuilder()
                .setID("txnId")
                .setNationsBenefitsGeneratedId("basTxnId")
                .setLocalTime(Timestamp.newBuilder().setSeconds(1609459200).setNanos(0).build())
                .setLocation(Address.newBuilder().setCountry("country").build())
                .setAmount(Amount.newBuilder().setAmount(1000).setSCurrencyCode("USD").build())
                .build();

        com.nationsbenefits.igloo.authengine.grpc.ISOMessage isoMessage = com.nationsbenefits.igloo.authengine.grpc.ISOMessage.newBuilder()
                .setTransaction(transaction)
                .setMessageType(com.nationsbenefits.igloo.authengine.grpc.MessageType.newBuilder().setMessageType("messageType").build())
                .setCard(Card.newBuilder().setPanHash("panHash").build())
                .setMerchant(Merchant.newBuilder().setCategoryCode("mccCode").build())
                .build();

        return com.nationsbenefits.igloo.authengine.grpc.AuthRequest.newBuilder().setIsoMessage(isoMessage).build();
    }

}