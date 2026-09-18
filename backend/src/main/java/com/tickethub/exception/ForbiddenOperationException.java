package com.tickethub.exception;

public class ForbiddenOperationException extends RuntimeException {
    /**
     * Javadoc.
      * @param message description
     */
    public ForbiddenOperationException(final String message) {
        super(message);
    }
}

