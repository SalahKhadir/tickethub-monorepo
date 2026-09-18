package com.tickethub.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    /**
     * Javadoc.
     */
    private long totalTickets;
    /**
     * Javadoc.
     */
    private long openTickets;
    /**
     * Javadoc.
     */
    private long resolvedToday;
    /**
     * Javadoc.
     */
    private long criticalSLA;
    /**
     * Javadoc.
     */
    private Map<String, Long> ticketsByCategory;
    /**
     * Javadoc.
     */
    private long totalUsers;
    /**
     * Javadoc.
     */
    private String avgResolutionTime;
}
