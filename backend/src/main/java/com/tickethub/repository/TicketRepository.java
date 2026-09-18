package com.tickethub.repository;

import com.tickethub.model.Priority;
import com.tickethub.model.Ticket;
import com.tickethub.model.TicketCategory;
import com.tickethub.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/*
 * COMPARAISON: J2EE CLASSIQUE vs SPRING DATA JPA
 *
 * Approche sans Spring :
 * Utilisation de JDBC pur avec des classes DAO. Écriture de requêtes SQL manuelles
 * (SELECT * FROM tickets...). Gestion des Connection, PreparedStatement et mapping
 * manuel du ResultSet vers les objets Java.
 *
 * Différence :
 * Spring Data JPA génère les requêtes à partir du nom des méthodes (Query Methods)
 * ou via @Query en JPQL.
 *
 * Avantage :
 * Réduction drastique du code répétitif ("boilerplate") et abstraction totale
 * de la base de données.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {
  /*
   * COMPARAISON PÉDAGOGIQUE: FILTRAGE DYNAMIQUE JDBC vs SPRING DATA JPA
   *
   * En JDBC classique, la gestion de filtres optionnels (status, priority, category)
   * aurait nécessité la construction manuelle d'une chaîne SQL dynamique avec des blocs "IF".
   * Par exemple :
   *   String sql = "SELECT * FROM tickets WHERE 1=1";
   *   if (status != null) { sql += " AND status = ?"; }
   *   if (priority != null) { sql += " AND priority = ?"; }
   *
   * Cela augmente drastiquement le risque d'erreurs de syntaxe, l'oubli d'espaces et
   * nécessite une gestion laborieuse de l'injection des paramètres (PreparedStatement.setDate(i, val)...).
   *
   * Grâce à Spring Data JPA (et JPQL/HQL), nous pouvons écrire une clause statique :
   * "(:param IS NULL OR t.field = :param)"
   * Hibernate s'occupe de compiler intelligemment la requête et de sécuriser l'injection
   * SQL automatiquement. Le code reste concis, propre et 100% sécurisé.
   */
  /**
   * Finds all tickets with filters.
   *
   * @param statuses description
   * @param priority description
   * @param category description
   * @param pageable description
   * @return description
   */
  @Query("""
      SELECT t
      FROM Ticket t
      WHERE (:statuses IS NULL OR t.status IN :statuses)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
      """)
  Page<Ticket> findAllWithFilters(
      @Param("statuses") List<TicketStatus> statuses,
      @Param("priority") Priority priority,
      @Param("category") TicketCategory category,
      Pageable pageable);

  /**
   * Finds tickets by author id with filters.
   *
   * @param authorId description
   * @param statuses description
   * @param priority description
   * @param category description
   * @param pageable description
   * @return description
   */
  @Query("""
      SELECT t
      FROM Ticket t
      WHERE t.author.id = :authorId
        AND (:statuses IS NULL OR t.status IN :statuses)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
      """)
  Page<Ticket> findByAuthorIdWithFilters(
      @Param("authorId") Long authorId,
      @Param("statuses") List<TicketStatus> statuses,
      @Param("priority") Priority priority,
      @Param("category") TicketCategory category,
      Pageable pageable);

  /**
   * Finds tickets by assigned technician id with filters.
   *
   * @param techId description
   * @param statuses description
   * @param priority description
   * @param category description
   * @param pageable description
   * @return description
   */
  @Query("""
      SELECT t
      FROM Ticket t
      WHERE t.assignedTechnician.id = :techId
        AND (:statuses IS NULL OR t.status IN :statuses)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:category IS NULL OR t.category = :category)
      """)
  Page<Ticket> findByAssignedTechnicianIdWithFilters(
      @Param("techId") Long techId,
      @Param("statuses") List<TicketStatus> statuses,
      @Param("priority") Priority priority,
      @Param("category") TicketCategory category,
      Pageable pageable);

  /**
   * Counts tickets assigned to a technician by status.
   *
   * @param techId technician identifier
   * @param statuses statuses to count
   * @return number of matching tickets
   */
  long countByAssignedTechnicianIdAndStatusIn(
      Long techId,
      java.util.List<TicketStatus> statuses);

  /**
   * Finds tickets nearing SLA.
   *
   * @param now description
   * @param threshold description
   * @param excludedStatuses description
   * @return description
   */
  @Query("""
      SELECT t
      FROM Ticket t
      WHERE t.slaDeadline BETWEEN :now AND :threshold
        AND t.status NOT IN :excludedStatuses
      """)
  java.util.List<Ticket> findTicketsNearingSla(
      @Param("now") java.time.LocalDateTime now,
      @Param("threshold") java.time.LocalDateTime threshold,
      @Param("excludedStatuses") java.util.List<TicketStatus> excludedStatuses);

  /**
   * Counts tickets assigned to a technician with a given status on or after
   * the specified start time.
   *
   * @param email technician email
   * @param status ticket status
   * @param startOfDay beginning of the reporting period
   * @return number of matching tickets
   */
  @Query("""
         SELECT COUNT(t)
         FROM Ticket t
         WHERE t.assignedTechnician.email = :email
           AND t.status = :status
           AND t.updatedAt >= :startOfDay
         """)
  long countByTechnicianAndStatusAndDate(
      @Param("email") String email,
      @Param("status") TicketStatus status,
      @Param("startOfDay") java.time.LocalDateTime startOfDay);

  /**
   * Counts tickets by technician email and statuses.
   *
   * @param email description
   * @param statuses description
   * @return description
   */
  long countByAssignedTechnicianEmailAndStatusIn(
      String email,
      List<TicketStatus> statuses);

  /**
   * Counts tickets by technician email and status.
   *
   * @param email description
   * @param status description
   * @return description
   */
  long countByAssignedTechnicianEmailAndStatus(String email, TicketStatus status);

  /**
   * Counts tickets by technician email, priority and statuses.
   *
   * @param email description
   * @param priority description
   * @param statuses description
   * @return description
   */
  long countByAssignedTechnicianEmailAndPriorityAndStatusIn(
      String email,
      Priority priority,
      List<TicketStatus> statuses);

  /**
   * Counts tickets by status.
   *
   * @param status description
   * @return description
   */
  long countByStatus(TicketStatus status);

  /**
   * Counts tickets by statuses.
   *
   * @param statuses description
   * @return description
   */
  long countByStatusIn(List<TicketStatus> statuses);

  /**
   * Counts tickets by priority.
   *
   * @param priority description
   * @return description
   */
  long countByPriority(Priority priority);

  /**
   * Counts tickets by status and date.
   *
   * @param status description
   * @param startOfDay description
   * @return description
   */
  @Query("SELECT COUNT(t) FROM Ticket t "
        + "WHERE t.status = :status "
        + "AND t.updatedAt >= :startOfDay")
  long countByStatusAndDate(
      @Param("status") TicketStatus status,
      @Param("startOfDay") java.time.LocalDateTime startOfDay);

  /**
   * Counts tickets by category.
   *
   * @return description
   */
  @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
  List<Object[]> countTicketsByCategoryGroup();

  /**
   * Finds all tickets by status.
   *
   * @param status description
   * @return description
   */
  List<Ticket> findAllByStatus(TicketStatus status);
}
