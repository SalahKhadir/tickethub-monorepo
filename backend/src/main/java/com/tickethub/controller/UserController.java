package com.tickethub.controller;

import com.tickethub.dto.response.TechnicianResponse;
import com.tickethub.model.Role;
import com.tickethub.model.User;
import com.tickethub.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
    /**
     * Javadoc.
     */
    private final UserRepository userRepository;
    /**
     * Javadoc.
     */
    private final com.tickethub.repository.TicketRepository ticketRepository;
    /**
     * Javadoc.
     */
    private final UserService userService;

    /**
     * Creates a user controller.
     *
     * @param pUserRepository repository for user operations
     * @param pTicketRepository repository for ticket operations
     * @param pUserService service for user operations
     */
    public UserController(
            final UserRepository pUserRepository,
            final com.tickethub.repository.TicketRepository pTicketRepository,
            final UserService pUserService) {
        this.userRepository = pUserRepository;
        this.ticketRepository = pTicketRepository;
        this.userService = pUserService;
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('TECH','ADMIN')")
    public ResponseEntity<List<TechnicianResponse>> getTechnicians() {
        return ResponseEntity.ok(toTechnicianResponses(userRepository.
            findByRole(Role.ROLE_TECH)));
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping("/users/technicians")
    @PreAuthorize("hasAnyRole('TECH','ADMIN')")
    public ResponseEntity<List<TechnicianResponse>> getTechniciansAlias() {
        return getTechnicians();
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping("/technicians/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.tickethub.dto.response.
        TechnicianAvailabilityResponse>> getTechniciansAvailability() {
        return ResponseEntity.ok(userService.getTechniciansAvailability());
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping("/admin/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TechnicianResponse>> getTechniciansAdmin() {
        return getTechnicians();
    }

    /**
     * Javadoc.
      * @param role description
      * @return description
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TechnicianResponse>> getUsers(
            @RequestParam(required = false) final String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.ok(toTechnicianResponses(userRepository.
                findAll()));
        }
        Role parsedRole = parseRole(role);
        return ResponseEntity.ok(toTechnicianResponses(userRepository.
            findByRole(parsedRole)));
    }

    // This method is commented out because it conflicts with
    // AdminUserController's @GetMapping
    // @GetMapping("/admin/users")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<List<TechnicianResponse>> getUsersAdmin(
    //         @RequestParam(required = false) String role) {
    //     return getUsers(role);
    // }

    private List<TechnicianResponse> toTechnicianResponses(
            final List<User> users) {
        return users.stream()
                .map(this::toTechnicianResponse)
                .collect(Collectors.toList());
    }

    private TechnicianResponse toTechnicianResponse(final User user) {
        String fullName = Stream.of(user.getPrenom(), user.getNom())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
        if (fullName.isBlank()) {
            fullName = user.getEmail();
        }
        return new TechnicianResponse(user.getId(), user.getEmail(), fullName);
    }

    private Role parseRole(final String roleValue) {
        String normalized = roleValue.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("TECHNICIAN") || normalized.equals("TECH")) {
            normalized = "ROLE_TECH";
        } else if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown role: " + roleValue);
        }
    }
}
