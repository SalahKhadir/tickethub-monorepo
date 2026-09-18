package com.tickethub.controller;

import com.tickethub.dto.request.TicketRequest;
import com.tickethub.dto.request.AssignRequest;
import com.tickethub.dto.request.TicketStatusUpdateRequest;
import com.tickethub.dto.request.TicketUpdateRequest;
import com.tickethub.dto.response.TicketResponse;
import com.tickethub.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.security.Principal;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    /**
     * Javadoc.
      * @return description
     */
    private final TicketService ticketService;

    /**
     * Javadoc.
      * @return description
      * @param request description
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','TECH','ADMIN')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody final TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.
            createTicket(request));
    }

    /**
     * Javadoc.
      * @return description
      * @param pageable description
      * @param category description
      * @param priority description
      * @param status description
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','TECH','ADMIN')")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String priority,
            @RequestParam(required = false) final String category,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.
                DESC)
            final Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAllTickets(pageable, status,
            priority, category));
    }

    /**
     * Javadoc.
      * @return description
      * @param principal description
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('TECH','ADMIN')")
    public ResponseEntity<com.tickethub.dto.response.TechnicianStatsResponse>
        getTechnicianStats(final Principal principal) {
        return ResponseEntity.ok(ticketService.getTechnicianStats(principal.
            getName()));
    }

    /**
     * Javadoc.
      * @return description
      * @param request description
      * @param id description
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECH','ADMIN')")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable final Long id,
            @Valid @RequestBody final TicketStatusUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request.
            newStatus(), request.solution()));
    }

    /**
     * Javadoc.
      * @return description
      * @param request description
      * @param id description
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('TECH','ADMIN')")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable final Long id,
            @Valid @RequestBody final AssignRequest request) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, request.
            techId()));
    }

    /**
     * Javadoc.
      * @return description
      * @param id description
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable final Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Javadoc.
      * @param request description
      * @param id description
     * @return description
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable final Long id,
            @RequestBody final TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }
}
