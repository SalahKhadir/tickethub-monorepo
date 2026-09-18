package com.tickethub.service;

import com.tickethub.dto.request.TicketRequest;
import com.tickethub.dto.request.TicketUpdateRequest;
import com.tickethub.dto.response.TicketResponse;
import com.tickethub.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    /**
     * Javadoc.
      * @return description
     * @param request description
     */
    TicketResponse createTicket(TicketRequest request);

    /**
     * Javadoc.
      * @return description
      * @param priorityString description
     * @param pageable description
     */
    Page<TicketResponse> getAllTickets(Pageable pageable, String statusString,
        String priorityString, String categoryString);

    /**
     * Javadoc.
      * @return description
     */
    TicketResponse getTicketById(Long id);

    /**
     * Javadoc.
      * @return description
     */
    TicketResponse assignTechnician(Long ticketId, Long techId);

    /**
     * Javadoc.
      * @param solution description
     * @return description
     */
    TicketResponse updateTicketStatus(Long id, TicketStatus newStatus, String
        solution);

    /**
     * Javadoc.
      * @return description
      * @param id description
     */
    TicketResponse updateTicket(Long id, TicketUpdateRequest request);

    /**
     * Javadoc.
      * @param id description
     */
    void deleteTicket(Long id);

    /**
     * Javadoc.
      * @param email description
      * @return description
     */
    com.tickethub.dto.response.TechnicianStatsResponse getTechnicianStats(
        String email);

    /**
     * Javadoc.
      * @return description
     */
    com.tickethub.dto.response.AdminStatsResponse getAdminGlobalStats();
}
