package com.tickethub.service.impl;

import com.tickethub.dto.request.TicketRequest;
import com.tickethub.dto.request.TicketUpdateRequest;
import com.tickethub.dto.response.TicketResponse;
import com.tickethub.exception.ForbiddenOperationException;
import com.tickethub.model.Priority;
import com.tickethub.exception.ResourceNotFoundException;
import com.tickethub.model.Role;
import com.tickethub.model.Ticket;
import com.tickethub.model.TicketCategory;
import com.tickethub.model.TicketStatus;
import com.tickethub.model.User;
import com.tickethub.repository.TicketRepository;
import com.tickethub.repository.UserRepository;
import com.tickethub.service.TicketService;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * COMPARAISON: J2EE CLASSIQUE vs SPRING BOOT (Services)
 *
 * Approche sans Spring :
 * Gestion manuelle des transactions via connection.setAutoCommit(false) et
 * connection.commit(). Instanciation manuelle des classes (pas d'Injection de
     Dépendances).
 *
 * Avantage Spring :
 * L'Inversion de Contrôle (IoC) et l'Injection de Dépendances (DI) rendent le
     code
 * modulaire, testable et découplé. L'annotation @Transactional gère les
     commits et rollbacks.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {
    /**
     * Javadoc.
     */
    private static final Set<String> CLIENT_AUTHORITIES = Set.of("ROLE_CLIENT");
    /**
     * Javadoc.
     */
    private static final Set<String> STAFF_AUTHORITIES = Set.of("ROLE_TECH",
        "ROLE_ADMIN");
    /**
     * Javadoc.
     */
    private static final Set<String> ADMIN_AUTHORITIES = Set.of("ROLE_ADMIN");
    /**
     * Javadoc.
     */
    private static final Set<String> TECH_AUTHORITIES = Set.of("ROLE_TECH");
    /**
     * Javadoc.
     */
    private static final long MILLIS_PER_HOUR = 3_600_000L;
    /**
     * Javadoc.
     */
    private static final long MILLIS_PER_MINUTE = 60_000L;

    /**
     * Javadoc.
     */
    private final TicketRepository ticketRepository;
    /**
     * Javadoc.
     */
    private final UserRepository userRepository;
    /**
     * Javadoc.
     */
    private final com.tickethub.service.NotificationPushService
        notificationPushService;

    /**
     * Creates a new ticket.
     *
     * @param request the ticket details
     * @return the created ticket response
     */
    @Override
    public TicketResponse createTicket(final TicketRequest request) {
        User currentUser = getCurrentUser();
        LocalDateTime slaDeadline = request.priority() == Priority.CRITICAL
                ? LocalDateTime.now().plusHours(2)
                : null;

        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .status(TicketStatus.NEW)
                .priority(request.priority())
                .category(request.category())
                .slaDeadline(slaDeadline)
                .author(currentUser)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        TicketResponse response = toResponse(savedTicket);

        notificationPushService.broadcastToAdmins(response);

        return response;
    }

    /**
     * Retrieves tickets visible to the authenticated user and applies the
     * requested pagination and filters.
     *
     * @param pageable pagination and sorting information
     * @param statusString comma-separated ticket statuses
     * @param priorityString ticket priority filter
     * @param categoryString ticket category filter
     * @return a page of ticket responses
     */
    @Override
    public Page<TicketResponse> getAllTickets(
            final Pageable pageable,
            final String statusString,
            final String priorityString,
            final String categoryString) {

        List<TicketStatus> statusList = null;
        if (statusString != null && !statusString.isBlank()) {
            statusList = Arrays.stream(statusString.split(","))
                    .map(String::trim)
                    .map(s -> {
                        try {
                            return TicketStatus.valueOf(s.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            if (statusList.isEmpty()) {
                statusList = null;
            }
        }

        Priority priority = null;
        if (priorityString != null && !priorityString.isBlank()) {
            try {
                priority = Priority.valueOf(priorityString.trim().toUpperCase(
                    ));
            } catch (IllegalArgumentException e) {
                // ignore invalid
            }
        }

        TicketCategory category = null;
        if (categoryString != null && !categoryString.isBlank()) {
            try {
                category = TicketCategory.valueOf(categoryString.trim().
                    toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore invalid
            }
        }

        Authentication authentication = getCurrentAuthentication();
        boolean isAdmin = hasAnyAuthority(authentication, ADMIN_AUTHORITIES);
        boolean isTech = hasAnyAuthority(authentication, TECH_AUTHORITIES);

        Page<Ticket> tickets;
        if (isAdmin) {
            tickets = ticketRepository.findAllWithFilters(statusList, priority,
                category, pageable);
        } else if (isTech) {
            User currentUser = getCurrentUser();
            tickets = ticketRepository.findByAssignedTechnicianIdWithFilters(
                    currentUser.getId(),
                    statusList,
                    priority,
                    category,
                    pageable);
        } else if (hasAnyAuthority(authentication, CLIENT_AUTHORITIES)) {
            User currentUser = getCurrentUser();
            tickets = ticketRepository.findByAuthorIdWithFilters(
                    currentUser.getId(),
                    statusList,
                    priority,
                    category,
                    pageable);
        } else {
            throw new ForbiddenOperationException("You are not allowed to access tickets.");
        }

        return tickets.map(this::toResponse);
    }

    /**
     * Retrieves a ticket by its ID.
     *
     * @param id the ID of the ticket
     * @return the ticket response
     */
    @Override
    public TicketResponse getTicketById(final Long id) {
        Ticket ticket = findTicketByIdOrThrow(id);
        Authentication authentication = getCurrentAuthentication();

        // Critical security check: only author or staff can access the ticket.
        boolean isStaff = hasAnyAuthority(authentication, STAFF_AUTHORITIES);
        boolean isAuthor = ticket.getAuthor().getEmail().equalsIgnoreCase(
            authentication.getName());
        if (!isAuthor && !isStaff) {
            throw new AccessDeniedException("You are not allowed to access this ticket.");
        }

        return toResponse(ticket);
    }

    /**
     * Assigns a technician to a ticket.
     *
     * @param ticketId the ID of the ticket
     * @param techId the ID of the technician
     * @return the updated ticket response
     */
    @Override
    @Transactional
    public TicketResponse assignTechnician(
            final Long ticketId,
            final Long techId) {
        Ticket ticket = findTicketByIdOrThrow(ticketId);
        Authentication authentication = getCurrentAuthentication();

        if (!hasAnyAuthority(authentication, ADMIN_AUTHORITIES)) {
            throw new AccessDeniedException("Only ADMIN can assign a technician.");
        }

        if (ticket.getStatus() != TicketStatus.ACCEPTED) {
            throw new IllegalStateException("Ticket must be ACCEPTED beforeassigning a technician.");
        }

        User technician = userRepository.findById(techId)
                .orElseThrow(() -> new ResourceNotFoundException("Techniciannot found: " + techId));

        if (!technician.getRoles().contains(Role.ROLE_TECH)) {
            throw new IllegalStateException("Assigned user must have roleTECHNICIAN.");
        }

        ticket.setAssignedTechnician(technician);
        // Status stays ACCEPTED — technician must click "Start Work" to move to IN_PROGRESS
        Ticket updatedTicket = ticketRepository.save(ticket);
        TicketResponse response = toResponse(updatedTicket);

        notificationPushService.push(technician.getEmail(), response);

        return response;
    }

    /**
     * Updates the status of a ticket.
     *
     * @param id the ID of the ticket
     * @param newStatus the new status
     * @param solution the solution description if resolved
     * @return the updated ticket response
     */
    @Override
    public TicketResponse updateTicketStatus(
            final Long id,
            final TicketStatus newStatus,
            final String solution) {
        Ticket ticket = findTicketByIdOrThrow(id);
        Authentication authentication = getCurrentAuthentication();

        TicketStatus currentStatus = ticket.getStatus();
        String currentUserEmail = authentication.getName();
        boolean isClient = hasAnyAuthority(authentication, CLIENT_AUTHORITIES);
        boolean isAuthor = ticket.getAuthor().getEmail().equalsIgnoreCase(
            currentUserEmail);

        if (currentStatus == TicketStatus.NEW && newStatus == TicketStatus.
            ACCEPTED) {
            if (!hasAnyAuthority(authentication, Set.of("ROLE_ADMIN"))) {
                throw new AccessDeniedException("Only ADMIN can accept a ticket.");
            }
        } else if (currentStatus == TicketStatus.ACCEPTED && newStatus ==
            TicketStatus.IN_PROGRESS) {
            if (!hasAnyAuthority(authentication, Set.of("ROLE_TECH"))) {
                throw new AccessDeniedException("Only TECH can start work on aticket.");
            }
            User currentUser = getCurrentUser();
            if (ticket.getAssignedTechnician() == null
                    || !ticket.getAssignedTechnician().getId().equals(
                        currentUser.getId())) {
                throw new AccessDeniedException(
                        "Only the assigned technician can start work on thisticket.");
            }
        } else if (currentStatus == TicketStatus.IN_PROGRESS && newStatus ==
            TicketStatus.RESOLVED) {
            if (!hasAnyAuthority(authentication, Set.of("ROLE_TECH"))) {
                throw new AccessDeniedException("Only TECH can resolve a ticket.");
            }
            User currentUser = getCurrentUser();
            if (ticket.getAssignedTechnician() == null
                    || !ticket.getAssignedTechnician().getId().equals(
                        currentUser.getId())) {
                throw new AccessDeniedException(
                        "Only the assigned technician can resolve this ticket.");
            }
            if (solution == null || solution.isBlank()) {
                throw new IllegalArgumentException("Solution is required toresolve a ticket.");
            }
            ticket.setSolution(solution);
        } else if (currentStatus == TicketStatus.RESOLVED && newStatus ==
            TicketStatus.CLOSED) {
            if (!(isClient && isAuthor)) {
                throw new AccessDeniedException("Only the CLIENT author canclose a ticket.");
            }
        } else {
            throw new IllegalStateException(
                    "Invalid transition: " + currentStatus + " -> " +
                        newStatus);
        }

        ticket.setStatus(newStatus);
        Ticket updatedTicket = ticketRepository.save(ticket);
        TicketResponse response = toResponse(updatedTicket);

        if (newStatus == TicketStatus.RESOLVED) {
            notificationPushService.push(ticket.getAuthor().getEmail(),
                response);
            notificationPushService.broadcastToAdmins(response);
        } else if (newStatus == TicketStatus.IN_PROGRESS) {
            notificationPushService.push(ticket.getAuthor().getEmail(),
                response);
            notificationPushService.broadcastToAdmins(response);
        }

        return response;
    }

    /**
     * Deletes a ticket by its ID.
     *
     * @param id the ID of the ticket
     */
    @Override
    @Transactional
    public void deleteTicket(final Long id) {
        Ticket ticket = findTicketByIdOrThrow(id);
        Authentication authentication = getCurrentAuthentication();

        // Critical security check: delete requires author ownership or staff role.
        boolean isStaff = hasAnyAuthority(authentication, STAFF_AUTHORITIES);
        boolean isClient = hasAnyAuthority(authentication, CLIENT_AUTHORITIES);
        boolean isAuthor = ticket.getAuthor().getEmail().equalsIgnoreCase(
            authentication.getName());

        if (!isAuthor && !isStaff) {
            throw new AccessDeniedException("You are not allowed to delete this ticket.");
        }

        if (isClient && ticket.getStatus() != TicketStatus.NEW) {
            throw new AccessDeniedException("A client can only delete a NEW ticket.");
        }

        // Clear assignment before delete to avoid FK constraint
        ticket.setAssignedTechnician(null);
        ticketRepository.saveAndFlush(ticket);
        ticketRepository.delete(ticket);
    }

    /**
     * Updates a ticket's basic information.
     *
     * @param id the ID of the ticket
     * @param request the update request
     * @return the updated ticket response
     */
    @Override
    @Transactional
    public TicketResponse updateTicket(final Long id, final TicketUpdateRequest
        request) {
        Ticket ticket = findTicketByIdOrThrow(id);
        Authentication authentication = getCurrentAuthentication();

        boolean isStaff = hasAnyAuthority(authentication, STAFF_AUTHORITIES);
        boolean isAuthor = ticket.getAuthor().getEmail()
                .equalsIgnoreCase(authentication.getName());

        if (!isAuthor && !isStaff) {
            throw new AccessDeniedException("You are not allowed to edit thisticket.");
        }
        if (isAuthor && !isStaff && ticket.getStatus() != TicketStatus.NEW) {
            throw new AccessDeniedException("You can only edit a ticket withstatus NEW.");
        }
        if (request.title() != null && !request.title().isBlank()) {
            ticket.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            ticket.setDescription(request.description().trim());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
            if (request.priority() == Priority.CRITICAL) {
                ticket.setSlaDeadline(LocalDateTime.now().plusHours(2));
            } else {
                ticket.setSlaDeadline(null);
            }
        }
        return toResponse(ticketRepository.save(ticket));
    }

    /**
     * Retrieves statistics for a specific technician.
     *
     * @param email the email of the technician
     * @return the technician's statistics
     */
    @Override
    public com.tickethub.dto.response.TechnicianStatsResponse
        getTechnicianStats(final String email) {
        /*
         * COMPARAISON: J2EE CLASSIQUE vs SPRING DATA JPA (Statistiques)
         *
         * En JDBC classique, il aurait fallu écrire 4 requêtes SELECT COUNT(*)
             distinctes
         * avec des jointures explicites pour vérifier l'email de l'utilisateur.
         * Avec Spring Data JPA, de simples requêtes basées sur le nom (Query
             Methods)
         * ou une courte @Query suffisent pour abstraire cette complexité d'un
             coup.
         */
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.now().with(
            java.time.LocalTime.MIN);

        long assignedTickets = ticketRepository.
            countByAssignedTechnicianEmailAndStatusIn(
                email, List.of(TicketStatus.ACCEPTED, TicketStatus.
                    IN_PROGRESS));

        long inProgress = ticketRepository.
            countByAssignedTechnicianEmailAndStatus(
                email, TicketStatus.IN_PROGRESS);

        long criticalPriority =
                ticketRepository.
                    countByAssignedTechnicianEmailAndPriorityAndStatusIn(
                        email,
                        Priority.CRITICAL,
                        List.of(
                                TicketStatus.ACCEPTED,
                                TicketStatus.IN_PROGRESS));

        long resolvedToday = ticketRepository.countByTechnicianAndStatusAndDate(
                email, TicketStatus.RESOLVED, startOfDay);

        return new com.tickethub.dto.response.TechnicianStatsResponse(
                assignedTickets, inProgress, criticalPriority, resolvedToday);
    }

    /**
     * Retrieves global statistics for administrators.
     *
     * @return global statistics response
     */
    @Override
    public com.tickethub.dto.response.AdminStatsResponse getAdminGlobalStats() {
        Authentication authentication = getCurrentAuthentication();
        if (!hasAnyAuthority(authentication, ADMIN_AUTHORITIES)) {
            throw new AccessDeniedException("Only ADMIN can view global stats.");
        }

        /*
         * COMPARAISON PÉDAGOGIQUE: RAPPORTS JDBC vs SPRING DATA JPA
         *
         * En JDBC classique, générer ces rapports aurait nécessité des clauses
             GROUP BY
         * complexes et de nombreux JOIN sur plusieurs tables (users, tickets,
             roles),
         * avec une itération manuelle sur un ResultSet pour construire la Map
             de retour.
         *
         * Avec Spring Data JPA, le framework permet de mapper ces résultats de
             groupe
         * directement en Map ou structures DTO grâce à HQL/JPQL ou aux méthodes
         * dérivées (Query Methods) très intuitives et puissantes.
         */

        long totalTickets = ticketRepository.count();

        long openTickets = ticketRepository.countByStatusIn(
                List.of(TicketStatus.NEW, TicketStatus.ACCEPTED, TicketStatus.
                    IN_PROGRESS)
        );

        LocalDateTime startOfDay = LocalDateTime.now().with(java.time.LocalTime.
            MIN);
        long resolvedToday = ticketRepository.countByStatusAndDate(
                TicketStatus.RESOLVED, startOfDay);

        long criticalSLA = ticketRepository.countByPriority(Priority.CRITICAL);

        List<Object[]> categoryCounts = ticketRepository.
            countTicketsByCategoryGroup();
        java.util.Map<String, Long> ticketsByCategory = categoryCounts.stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? row[0].toString() : "UNKNOWN",
                        row -> ((Number) row[1]).longValue()
                ));

        long totalUsers = userRepository.count();

        List<Ticket> resolved = ticketRepository.findAllByStatus(TicketStatus.
            RESOLVED);
        String avgResolutionTime = "N/A";
        if (!resolved.isEmpty()) {
            long totalMs = resolved.stream()
                .filter(t -> t.getCreatedAt() != null && t.getUpdatedAt() !=
                    null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.
                    getUpdatedAt()).toMillis())
                .sum();
            long avgMs = totalMs / resolved.size();
            long hours = avgMs / MILLIS_PER_HOUR;
            long mins  = (avgMs % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE;
            avgResolutionTime = hours + "h " + mins + "m";
        }

        return new com.tickethub.dto.response.AdminStatsResponse(
                totalTickets, openTickets, resolvedToday, criticalSLA,
                    ticketsByCategory, totalUsers, avgResolutionTime);
    }

    private User getCurrentUser() {
        Authentication authentication = getCurrentAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    private Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().
            getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getName())) {
            throw new ForbiddenOperationException("Authentication is required.");
        }
        return authentication;
    }

    private boolean hasAnyAuthority(final Authentication authentication, final
        Set<String> allowedAuthorities) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(allowedAuthorities::contains);
    }

    private Ticket findTicketByIdOrThrow(final Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    private TicketResponse toResponse(final Ticket ticket) {
        User author = ticket.getAuthor();
        String authorName = buildAuthorName(author);
        String assigneeName = ticket.getAssignedTechnician() == null
                ? null
                : buildAuthorName(ticket.getAssignedTechnician());

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getSlaDeadline(),
                ticket.getSolution(),
                authorName,
                assigneeName);
    }

    private String buildAuthorName(final User author) {
        String firstName = author.getPrenom() == null ? "" : author.getPrenom().
            trim();
        String lastName = author.getNom() == null ? "" : author.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? author.getEmail() : fullName;
    }
}
