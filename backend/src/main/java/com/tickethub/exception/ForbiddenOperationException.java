package com.tickethub.exception;

public class ForbiddenOperationException extends RuntimeException {
    /**
     * Javadoc.
      * @param message description
     */
    public ForbiddenOperationException(String message) {
        super(message);
    }
}

