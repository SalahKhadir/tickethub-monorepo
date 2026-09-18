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
    private static final int MAX_STRING_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_TEL_LENGTH = 30;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = MAX_STRING_LENGTH)
    private String nom;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = MAX_STRING_LENGTH)
    private String prenom;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(max = MAX_TEL_LENGTH)
    private String tel;

    /**
     * Javadoc.
     */
    @NotBlank
    @Email
    @Size(max = MAX_EMAIL_LENGTH)
    private String email;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_STRING_LENGTH)
    private String password;

    /**
     * Javadoc.
     */
    @NotBlank
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_STRING_LENGTH)
    private String retypePassword;
}

