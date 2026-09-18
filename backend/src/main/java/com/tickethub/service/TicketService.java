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
      * @param categoryString description
      * @return description
      * @param pageable description
      * @param statusString description
     */
    TicketResponse createTicket(TicketRequest request);

    /**
     * Javadoc.
      * @return description
      * @param id description
      * @param priorityString description
     */
    Page<TicketResponse> getAllTickets(Pageable pageable, String statusString,
        String priorityString, String categoryString);

    /**
     * Javadoc.
      * @return description
      * @param ticketId description
      * @param techId description
     */
    TicketResponse getTicketById(Long id);

    /**
     * Javadoc.
      * @return description
      * @param id description
      * @param newStatus description
     */
    TicketResponse assignTechnician(Long ticketId, Long techId);

    /**
     * Javadoc.
      * @param request description
      * @param solution description
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
