package com.tickethub.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public final class JwtTokenProvider {
    /**
     * Javadoc.
     */
    private static final int TOKEN_SUBSTRING_START = 3;

    /**
     * Javadoc.
     */
    private static final int ADMIN_ROLE_WEIGHT = 3;
    /**
     * Javadoc.
     */
    private static final int TECH_ROLE_WEIGHT = 2;
    /**
     * Javadoc.
     */
    private static final int CLIENT_ROLE_WEIGHT = 1;

    /**
     * Javadoc.
     */
    private final SecretKey secretKey;
    /**
     * Javadoc.
     */
    private final long jwtExpirationMs;

    /**
     * Javadoc.
      * @param pJwtExpirationMs description
      * @param jwtSecret description
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") final String jwtSecret,
            @Value("${app.jwt.expiration-ms}") final long pJwtExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.
            UTF_8));
        this.jwtExpirationMs = pJwtExpirationMs;
    }

    /**
     * Javadoc.
      * @return description
      * @param authentication description
     */
    public String generateToken(final Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted((r1, r2) -> {
                    int w1 = r1.equals("ROLE_ADMIN")
                            ? ADMIN_ROLE_WEIGHT
                            : r1.equals("ROLE_TECH")
                                    ? TECH_ROLE_WEIGHT
                                    : CLIENT_ROLE_WEIGHT;

                    int w2 = r2.equals("ROLE_ADMIN")
                            ? ADMIN_ROLE_WEIGHT
                            : r2.equals("ROLE_TECH")
                                    ? TECH_ROLE_WEIGHT
                                    : CLIENT_ROLE_WEIGHT;
                    return Integer.compare(w2, w1);
                })
                .findFirst()
                .orElse("ROLE_CLIENT");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Javadoc.
      * @return description
      * @param token description
     */
    public String getUsernameFromToken(final String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Javadoc.
      * @return description
      * @param token description
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
