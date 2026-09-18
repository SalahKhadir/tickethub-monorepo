package com.tickethub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianStatsResponse {
    /**
     * Javadoc.
     */
    private long assignedTickets;
    /**
     * Javadoc.
     */
    private long inProgress;
    /**
     * Javadoc.
     */
    private long criticalPriority;
    /**
     * Javadoc.
     */
    private long resolvedToday;
}

