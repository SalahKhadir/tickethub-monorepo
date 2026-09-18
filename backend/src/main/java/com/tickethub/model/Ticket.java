package com.tickethub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tickets")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_ENUM_LENGTH = 20;
    /**
     * Javadoc.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Javadoc.
     */
    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    /**
     * Javadoc.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Javadoc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MAX_ENUM_LENGTH)
    private TicketStatus status;

    /**
     * Javadoc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MAX_ENUM_LENGTH)
    private Priority priority;

    /**
     * Javadoc.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MAX_ENUM_LENGTH)
    private TicketCategory category;

    /**
     * Javadoc.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Javadoc.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Javadoc.
     */
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    /**
     * Javadoc.
     */
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    /*
     * COMPARAISON: J2EE CLASSIQUE vs SPRING DATA JPA (Relations)
     *
     * Approche sans Spring :
     * Gestion manuelle des clés étrangères en SQL. Pour récupérer un ticket
         avec
     * son
     * auteur, il faudrait faire une jointure manuelle (JOIN) et reconstruire
     * l'arborescence
     * des objets en Java.
     *
     * Différence :
     * JPA gère les relations via des annotations et automatise le chargement
         des
     * objets
     * liés (Lazy/Eager loading).
     */
    /**
     * Javadoc.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Javadoc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private User assignedTechnician;

    /**
     * Javadoc.
     */
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TicketStatus.NEW;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
