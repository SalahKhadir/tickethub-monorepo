package com.tickethub.service;

import com.tickethub.dto.request.LoginRequest;
import com.tickethub.dto.request.RegisterRequest;
import com.tickethub.dto.response.JwtResponse;
import com.tickethub.model.Role;
import com.tickethub.model.User;
import com.tickethub.repository.UserRepository;
import com.tickethub.security.jwt.JwtTokenProvider;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    /**
     * Javadoc.
     */
    private final AuthenticationManager authenticationManager;
    /**
     * Javadoc.
     */
    private final JwtTokenProvider jwtTokenProvider;
    /**
     * Javadoc.
     */
    private final UserRepository userRepository;
    /**
     * Javadoc.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates an authentication service.
     *
     * @param pAuthenticationManager authentication manager
     * @param pJwtTokenProvider JWT token provider
     * @param pUserRepository user repository
     * @param pPasswordEncoder password encoder
     */
    public AuthService(
            final AuthenticationManager pAuthenticationManager,
            final JwtTokenProvider pJwtTokenProvider,
            final UserRepository pUserRepository,
            final PasswordEncoder pPasswordEncoder) {
        this.authenticationManager = pAuthenticationManager;
        this.jwtTokenProvider = pJwtTokenProvider;
        this.userRepository = pUserRepository;
        this.passwordEncoder = pPasswordEncoder;
    }

    /**
     * Javadoc.
      * @return description
      * @param loginRequest description
     */
    public JwtResponse login(final LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return new JwtResponse(token, "Bearer", authentication.getName(),
            roles);
    }

    /**
     * Javadoc.
      * @param registerRequest description
     */
    public void registerClient(final RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken.");
        }

        if (!registerRequest.getPassword().equals(registerRequest.
            getRetypePassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User();
        user.setNom(registerRequest.getNom());
        user.setPrenom(registerRequest.getPrenom());
        user.setTel(registerRequest.getTel());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(Role.ROLE_CLIENT));
        user.setEnabled(false);

        userRepository.save(user);
    }
}
