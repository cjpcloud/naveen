package com.nationsbenefits.igloo.authengine.exception;

/**
 *
 *  @author PwC
 *  Copyright Â© 2024, NationsBenefits. All Rights reserved
 *
 * The AuthEngineCircuitBreakerFallbackException class representing a fallback exception triggered by a circuit breaker in the Auth Engine.
 * This exception is thrown when the circuit breaker opens and a fallback method is invoked.
 */
public class AuthEngineCircuitBreakerFallbackException extends RuntimeException{

    /**
     * Constructs a new AuthEngineCircuitBreakerFallbackException with the specified underlying exception.
     *
     * @param ex the underlying exception that caused this exception
     */
    public AuthEngineCircuitBreakerFallbackException(Exception ex) {
        super(ex);
    }

    /**
     * Constructs a new AuthEngineCircuitBreakerFallbackException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AuthEngineCircuitBreakerFallbackException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthEngineCircuitBreakerFallbackException with the specified detail message and cause.
     *
     * @param message   the detail message explaining the reason for the exception
     * @param throwable the cause of this exception
     */
    public AuthEngineCircuitBreakerFallbackException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
