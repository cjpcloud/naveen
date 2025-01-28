/**
 * Copyright © 2024, NationsBenefits. All Rights Reserved.
 */
package com.nationsbenefits.igloo.authengine;

import com.nationsbenefits.igloo.config.ConfigurationInitializer;
import com.nationsbenefits.igloo.event.publisher.annotation.EnableEventPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Main application class for the Igloo Auth Engine Service.
 *
 * <p>This class serves as the entry point for the Spring Boot application. The Igloo Auth Engine Service
 * supports receiving requests to perform authorization, balance inquiry, reversals, and chargebacks
 * for both MasterCard and NYCE payment networks.</p>
 *
 * <p>The service holds the gRPC endpoints for authorization, balance inquiry, reversal, and chargebacks.</p>
 *
 *
 * @author PwC
 */
@SpringBootApplication
@EnableEventPublisher
public class IglooAuthEngineApplication {

	/**
	 * Main method that serves as the entry point for the Spring Boot application.
	 *
	 * <p>This method starts the Spring Boot application by invoking {@link SpringApplication#run(Class, String...)}
	 * with the current class and command line arguments. It initializes the Spring application context
	 * and starts the embedded web server.</p>
	 *
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {
		new SpringApplicationBuilder(IglooAuthEngineApplication.class).initializers(new ConfigurationInitializer())
				.run(args);
	}
}
