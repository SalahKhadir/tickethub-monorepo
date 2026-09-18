import re

filepath = "src/main/java/com/tickethub/service/TicketService.java"
with open(filepath, 'r') as f:
    content = f.read()

# Fix getAllTickets
content = content.replace("""    /**
     * Javadoc.
      * @return description
      * @param priorityString description
     * @param pageable description
     */
    Page<TicketResponse> getAllTickets(Pageable pageable, String statusString,
        String priorityString, String categoryString);""",
"""    /**
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
            String categoryString);""")

# Fix getTicketById
content = content.replace("""    /**
     * Javadoc.
      * @return description
     */
    TicketResponse getTicketById(Long id);""",
"""    /**
     * Retrieves a ticket by identifier.
     *
     * @param id ticket identifier
     * @return the ticket response
     */
    TicketResponse getTicketById(Long id);""")

# Fix assignTechnician
content = content.replace("""    /**
     * Javadoc.
      * @return description
     */
    TicketResponse assignTechnician(Long ticketId, Long techId);""",
"""    /**
     * Assigns a technician to a ticket.
     *
     * @param ticketId ticket identifier
     * @param techId technician identifier
     * @return the updated ticket
     */
    TicketResponse assignTechnician(Long ticketId, Long techId);""")

# Fix updateTicketStatus
content = content.replace("""    /**
     * Javadoc.
      * @param solution description
     * @return description
     */
    TicketResponse updateTicketStatus(Long id, TicketStatus newStatus, String
        solution);""",
"""    /**
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
            String solution);""")

# Fix updateTicket
content = content.replace("""    /**
     * Javadoc.
      * @return description
      * @param id description
     */
    TicketResponse updateTicket(Long id, TicketUpdateRequest request);""",
"""    /**
     * Updates ticket details.
     *
     * @param id ticket identifier
     * @param request update request
     * @return the updated ticket
     */
    TicketResponse updateTicket(Long id, TicketUpdateRequest request);""")

with open(filepath, 'w') as f:
    f.write(content)
