package com.nationsbenefits.igloo.iso8583.adapter.exception;

/**
 *
 *  @author PwC
 *  Copyright Â© 2024, NationsBenefits. All Rights reserved
 *
 * The ISOAdapterCircuitBreakerFallbackException class representing a fallback exception triggered by a circuit breaker in the ISO Adapter.
 * This exception is thrown when the circuit breaker opens and a fallback method is invoked.
 */
public class ISOAdapterCircuitBreakerFallbackException extends RuntimeException{

    /**
     * Constructs a new ISOAdapterCircuitBreakerFallbackException with the specified underlying exception.
     *
     * @param ex the underlying exception that caused this exception
     */
    public ISOAdapterCircuitBreakerFallbackException(Exception ex) {
        super(ex);
    }

    /**
     * Constructs a new ISOAdapterCircuitBreakerFallbackException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ISOAdapterCircuitBreakerFallbackException(String message) {
        super(message);
    }

    /**
     * Constructs a new ISOAdapterCircuitBreakerFallbackException with the specified detail message and cause.
     *
     * @param message   the detail message explaining the reason for the exception
     * @param throwable the cause of this exception
     */
    public ISOAdapterCircuitBreakerFallbackException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
