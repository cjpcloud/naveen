package com.nationsbenefits.igloo.authengine.constant;


/**
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * This is the Constant Class to store all error constants to use in Auth Engine service for gRPC errors
 */
public interface ErrorConstant {

    /**
     * Error code for when a service is unavailable.
     */
    public static final String UNAVAILABLE = "UNAVAILABLE";

    /**
     * Error code for when a deadline is exceeded.
     */
    public static final String DEADLINE_EXCEEDED = "DEADLINE_EXCEEDED";

    /**
     * Error code for when a resource is not found.
     */
    public static final String NOT_FOUND = "NOT_FOUND";

    /**
     * Error code for when permission is denied.
     */
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    /**
     * Error code for when resources are exhausted.
     */
    public static final String RESOURCE_EXHAUSTED = "RESOURCE_EXHAUSTED";

    /**
     * Error code for when a method is unimplemented.
     */
    public static final String UNIMPLEMENTED = "UNIMPLEMENTED";

    /**
     * Error code for when data loss occurs.
     */
    public static final String DATA_LOSS = "DATA_LOSS";

    /**
     * Error code for when a internal error occurs.
     */
    public static final String INTERNAL = "INTERNAL";
}
