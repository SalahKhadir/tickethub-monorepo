package com.tickethub.controller;

import com.tickethub.dto.request.RegisterRequest;
import com.tickethub.dto.response.UserSummaryDTO;
import com.tickethub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminUserController {
    /**
     * Javadoc.
     */
    private final UserService userService;

    /**
     * Javadoc.
      * @param pUserService description
     */
    public AdminUserController(final UserService pUserService) {
        this.userService = pUserService;
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Javadoc.
      * @return description
     */
    @GetMapping("/pending")
    public ResponseEntity<List<UserSummaryDTO>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }

    /**
     * Javadoc.
      * @param id description
      * @return description
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable final Long id) {
        userService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Javadoc.
      * @param request description
      * @return description
     */
    @PostMapping
    public ResponseEntity<Void> createUserByAdmin(@Valid @RequestBody
        RegisterRequest request) {
        userService.createUserByAdmin(request);
        return ResponseEntity.ok().build();
    }
}
