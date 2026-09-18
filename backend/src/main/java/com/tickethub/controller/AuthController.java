package com.tickethub.controller;

import com.tickethub.dto.request.LoginRequest;
import com.tickethub.dto.request.RegisterRequest;
import com.tickethub.dto.response.JwtResponse;
import com.tickethub.dto.response.RegistrationResponse;
import com.tickethub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    /**
     * Javadoc.
     */
    private final AuthService authService;

    /**
     * Javadoc.
      * @param authService description
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Javadoc.
      * @param loginRequest description
      * @return description
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest
        loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    /**
     * Javadoc.
      * @param registerRequest description
      * @return description
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody
        final RegisterRequest registerRequest) {
        authService.registerClient(registerRequest);
        RegistrationResponse response = new RegistrationResponse(
                "Registration successful. Your account is pending admin approval.",
                true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
