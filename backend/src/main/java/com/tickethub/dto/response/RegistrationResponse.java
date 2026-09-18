package com.tickethub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationResponse {
    /**
     * Javadoc.
     */
    private String message;
    /**
     * Javadoc.
     */
    private boolean pendingApproval;
}

