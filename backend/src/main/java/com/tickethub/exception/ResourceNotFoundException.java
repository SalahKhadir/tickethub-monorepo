package com.tickethub.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Javadoc.
      * @param message description
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}

