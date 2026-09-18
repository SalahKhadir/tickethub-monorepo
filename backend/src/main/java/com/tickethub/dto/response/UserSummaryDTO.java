package com.tickethub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    /**
     * Javadoc.
     */
    private Long id;
    /**
     * Javadoc.
     */
    private String email;
    /**
     * Javadoc.
     */
    private String fullName;
    /**
     * Javadoc.
     */
    private String nom;
    /**
     * Javadoc.
     */
    private String prenom;
    /**
     * Javadoc.
     */
    private String tel;
    /**
     * Javadoc.
     */
    private String role;
    /**
     * Javadoc.
     */
    private boolean enabled;
    /**
     * Javadoc.
     */
    private LocalDateTime createdAt;
}
