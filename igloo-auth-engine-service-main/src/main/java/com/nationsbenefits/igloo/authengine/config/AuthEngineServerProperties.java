package com.nationsbenefits.igloo.authengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>AuthEngineServerProperties</h1>
 * This AuthEngineServerProperties is model class used to store AuthEngine service config details
 *
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "service")
public class AuthEngineServerProperties {

    private Host host;
    private Port port;

    /**
     * The initial number of threads to be created in pool.
     */
    private int initialThreadPoolCount;

    /**
     * The maximum number of threads to be created in pool.
     */
    private int maxThreadPoolCount;

    /**
     * The keepAliveTime configuration
     */
    private int keepAliveTime;

    /**
     * The keepAliveTimeout configuration
     */
    private int keepAliveTimeout;

    /**
     * The deadlineTimeout configuration
     */
    private int deadlineTimeout;

    @Setter
    @Getter
    public static class Host {
        private String card;
        private String txnFraud;
        private String member;
        private String memberAccount;
        private String BAS;
        private String ledger;


    }

    @Setter
    @Getter
    public static class Port {
        private int card;
        private int txnFraud;
        private int member;
        private int memberAccount;
        private int BAS;
        private int ledger;



    }

}
