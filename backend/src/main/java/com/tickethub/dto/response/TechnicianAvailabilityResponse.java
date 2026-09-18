package com.tickethub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianAvailabilityResponse {
    /**
     * Javadoc.
     */
    private Long id;
    /**
     * Javadoc.
     */
    private String nom;
    /**
     * Javadoc.
     */
    private String prenom;
    /**
     * Javadoc.
     */
    private String email;
    /**
     * Javadoc.
     */
    private String fullName;
    /**
     * Javadoc.
     */
    private long activeTicketsCount;
}
