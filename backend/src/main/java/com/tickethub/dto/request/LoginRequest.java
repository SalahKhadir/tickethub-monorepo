package com.tickethub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    /**
     * Javadoc.
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Javadoc.
     */
    @NotBlank
    private String password;
}
