package com.tickethub.controller;

import com.tickethub.dto.request.AssignRequest;
import com.tickethub.dto.response.TicketResponse;
import com.tickethub.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {
    /**
     * Javadoc.
      * @return description
     */
    private final TicketService ticketService;

    /**
     * Javadoc.
      * @param request description
      * @param id description
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable final Long id,
            @Valid @RequestBody final AssignRequest request) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, request.
            techId()));
    }
}

