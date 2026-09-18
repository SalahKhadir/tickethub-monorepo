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
     * Retrieves tickets using optional filters.
     *
     * @param pageable pagination information
     * @param statusString status filter
     * @param priorityString priority filter
     * @param categoryString category filter
     * @return a page of tickets
     */
    Page<TicketResponse> getAllTickets(
            Pageable pageable,
            String statusString,
            String priorityString,
            String categoryString);

    /**
     * Retrieves a ticket by identifier.
     *
     * @param id ticket identifier
     * @return the ticket response
     */
    TicketResponse getTicketById(Long id);

    /**
     * Assigns a technician to a ticket.
     *
     * @param ticketId ticket identifier
     * @param techId technician identifier
     * @return the updated ticket
     */
    TicketResponse assignTechnician(Long ticketId, Long techId);

    /**
     * Updates a ticket status.
     *
     * @param id ticket identifier
     * @param newStatus new status
     * @param solution resolution text
     * @return the updated ticket
     */
    TicketResponse updateTicketStatus(
            Long id,
            TicketStatus newStatus,
            String solution);

    /**
     * Updates ticket details.
     *
     * @param id ticket identifier
     * @param request update request
     * @return the updated ticket
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
