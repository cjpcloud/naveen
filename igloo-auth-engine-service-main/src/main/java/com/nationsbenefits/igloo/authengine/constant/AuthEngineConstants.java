/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine.constant;

/**
 * Class to hold all the constant values to be used in Auth Engine Service.
 * This class contains various constants that are used throughout the Auth Engine Service
 * for maintaining consistency and avoiding hard-coded values.
 *
 * <p>The class is designed to be non-instantiable by having a private constructor,
 * ensuring that it only serves as a container for constants.</p>
 *
 * @author PwC
 */
public interface AuthEngineConstants {

    /**
     * Constant representing a declined authentication response "DECLINED".
     */
    public static final String DECLINED_AUTH_RESPONSE = "DECLINED";

    public static final String APPROVED_AUTH_RESPONSE = "APPROVED";

    /**
     * Constant representing the field identifier for PAN (Primary Account Number).
     */
    public static final String PAN = "pan hash";

    /**
     * Constant representing the field identifier for member ID.
     */
    public static final String MEMBER_ID = "memberId";

    /**
     * Constant representing the field identifier for member Account ID.
     */
    public static final String MEMBER_ACCOUNT_ID = "MemberAccountID";

    /**
     * Constant representing the prefix for gRPC thread names.
     */
    public static final String GRPC_THREAD_NAME = "GrpcThread-";

    public static final String BAS_MERCHANT_INVALID = "206";
    public static final String BAS_TRANSACTION_LIMIT_EXCEED = "203";
    public static final String BAS_ALLOW = "000";
    public static final String BAS_TRANSACTION_PARTIALLY_APPROVED = "100";
    public static final String BAS_PRODUCTS_DECLINED = "200";
    public static final String BAS_INSUFFICIENT_FUND = "123";
    public static final String BAS_UNAUTHORIZED_STORE_LOCATION = "205";
    public static final String CARD_SERVICE = "CardService";
    public static final String TXN_FRAUD_SERVICE = "TxnFraudService";
    public static final String MEMBER_SERVICE = "MemberService";
    public static final String MEMBER_ACCOUNT_SERVICE = "MemberAccountService";
    public static final String BAS_SERVICE = "BASService";
    public static final String LEDGER_SERVICE = "LedgerService";

    public static final String BAS_INVALID_MERCHANT_CODE = "206";

    public static final String BAS_TRANSACTION_LIMIT_CODE = "203";

    public static final String BAS_SUCCESS = "000";

    public static final String BAS_PARTIAL_ALLOW_CODE = "100";

    public static final String BAS_PRODUCT_DECLINED_CODE = "200";

    public static final String BAS_INSUFFICIENT_FUND_CODE = "123";

    public static final String BAS_UNAUTHORIZED_LOCATION_CODE = "205";

    public static final String BAS_INVALID_MERCHANT_DESC = "Merchant is invalid";

    public static final String BAS_TRANSACTION_LIMIT_DESC = "Transaction entirely declined due to transaction limit exceeded";

    public static final String BAS_ADJ_SUCCESS_DESC = "BAS Adjudication successful";

    public static final String BAS_SUCCESS_DESC = "Transaction fully approved; more details at line items";

    public static final String BAS_AUTH_SUCCESS_DESC = "BAS Authorization successful";

    public static final String BAS_PARTIAL_ALLOW_DESC= "Transaction partially approved; more details at line items";

    public static final String BAS_PRODUCT_DECLINED_DESC = "Transaction entirely declined due to unauthorized products in the basket";

    public static final String BAS_INSUFFICIENT_FUND_DESC = "Transaction entirely declined due to no funds in the purses";

    public static final String BAS_UNAUTHORIZED_LOCATION_DESC = "Transaction entirely declined due to purchase at an unauthorized store location or online retailer or country";

    public static final String BAS_TRANSACTION_DECLINED = "Transaction declined";

    public static final String CARD_NUMBER_INVALID_DESC ="Card data is not found for the PAN Hash";

    public static final String CARD_NOT_ACTIVATED_DESC ="Card is found but Status is Inactive";

    public static final String CARD_LOCKED_DESC ="Card is in locked status";

    public static final String CARD_AUTHENTICATION_FAILED_DESC ="Card found and card type is NOT debit but card status is active";

    public static final String CARD_CVV_MISMATCH_DESC ="Card found and Status is active but PIN didn't match";

    public static final String CARD_PIN_VALIDATION_FAILURE_DESC ="Card found and Status is active but PIN didn't match";

    public static final String CARD_SUCCESS ="Card authorization successful";

    public static final String CARD_EXPIRED_DESC ="Card is found but expired";

    public static final String CARD_NUMBER_INVALID_CODE ="14";

    public static final String CARD_NOT_ACTIVATED_CODE ="78";

    public static final String CARD_LOCKED_CODE ="38";

    public static final String CARD_AUTHENTICATION_FAILED_CODE ="82";

    public static final String CARD_CVV_MISMATCH_CODE ="82";

    public static final String CARD_PIN_VALIDATION_FAILURE_CODE ="55";

    public static final String CARD_EXPIRED_CODE ="54";

    public static final String FRAUD_SUSPECT_CODE = "206";

    public static final String FRAUD_SUSPECT_DESC = "Transaction entirely declined due to suspected fraud";

    public static final String LEDGER_INSUFFICIENT_FUND = "Ledger Validation Failed: Insufficient Funds";

    public static final String LEDGER_ACCOUNT_NOT_FOUND = "Ledger Validation Failed: Account Not Found";
    public static final String LEDGER_VALIDATION_FAILED = "Ledger Validation Failed";
    public static final String LEDGER_TRANSACTION_POSTED = "Ledger Posting Successful.";



}
