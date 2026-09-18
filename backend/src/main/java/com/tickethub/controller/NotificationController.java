package com.tickethub.controller;

import com.tickethub.service.NotificationPushService;
import com.tickethub.repository.UserRepository;
import com.tickethub.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

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
      * @return description
     */
    private final NotificationPushService notificationPushService;

    /**
     * Javadoc.
      * @param response description
      * @param token description
     * @return description
     */
    @GetMapping(value = "/subscribe", produces = MediaType.
        TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("token") final String token,
        final HttpServletResponse response) {
        System.out.println("SSE Subscription Attempted");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String email = jwtTokenProvider.getUsernameFromToken(token);

        // Ensure user actually exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User not found"));

        return notificationPushService.subscribe(email);
    }
}
