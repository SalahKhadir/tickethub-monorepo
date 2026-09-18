package com.tickethub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = 100)
    private String nom;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = 100)
    private String prenom;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = 30)
    private String tel;

    /**
     * Javadoc.
     */
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(min = 8, max = 100)
    private String retypePassword;
}

