package com.tickethub.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    /**
     * Javadoc.
     */
    private String accessToken;
    /**
     * Javadoc.
     */
    private String tokenType;
    /**
     * Javadoc.
     */
    private String email;
    /**
     * Javadoc.
     */
    private List<String> roles;
}
