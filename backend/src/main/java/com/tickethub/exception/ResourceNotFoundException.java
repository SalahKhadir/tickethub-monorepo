package com.tickethub.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Javadoc.
      * @param message description
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

