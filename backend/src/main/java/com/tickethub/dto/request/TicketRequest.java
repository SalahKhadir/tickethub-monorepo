package com.tickethub.dto.request;

import com.tickethub.model.Priority;
import com.tickethub.model.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotBlank
        @Size(max = MAX_TITLE_LENGTH)
        String title,

        @NotBlank
        String description,

        @NotNull
        Priority priority,

        @NotNull
        TicketCategory category) {

    /** Maximum allowed title length for ticket creation requests. */
    private static final int MAX_TITLE_LENGTH = 255;
}

