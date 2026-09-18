import re

filepath = "src/main/java/com/tickethub/repository/TicketRepository.java"
with open(filepath, 'r') as f:
    content = f.read()

# Fix block comment 1
old_comment1 = """ * Approche sans Spring :
 * Utilisation de JDBC pur avec des classes DAO. Écriture de requêtes SQL manuelles
 * (SELECT * FROM tickets...). Gestion des Connection, PreparedStatement et mapping
 * manuel du ResultSet vers les objets Java.
 *
 * Différence :
 * Spring Data JPA génère les requêtes à partir du nom des méthodes (Query Methods)
 * ou via @Query en JPQL."""
new_comment1 = """ * Approche sans Spring :
 * Utilisation de JDBC pur avec des classes DAO. Écriture de requêtes SQL
 * manuelles (SELECT * FROM tickets...). Gestion des Connection,
 * PreparedStatement et mapping manuel du ResultSet vers les objets Java.
 *
 * Différence :
 * Spring Data JPA génère les requêtes à partir du nom des méthodes
 * (Query Methods) ou via @Query en JPQL."""
content = content.replace(old_comment1, new_comment1)

# Fix block comment 2
old_comment2 = """   * En JDBC classique, la gestion de filtres optionnels (status, priority, category)
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
   * SQL automatiquement. Le code reste concis, propre et 100% sécurisé."""
new_comment2 = """   * En JDBC classique, la gestion de filtres optionnels (status, priority,
   * category) aurait nécessité la construction manuelle d'une chaîne SQL
   * dynamique avec des blocs "IF".
   * Par exemple :
   *   String sql = "SELECT * FROM tickets WHERE 1=1";
   *   if (status != null) { sql += " AND status = ?"; }
   *   if (priority != null) { sql += " AND priority = ?"; }
   *
   * Cela augmente drastiquement le risque d'erreurs de syntaxe, l'oubli
   * d'espaces et nécessite une gestion laborieuse de l'injection des
   * paramètres (PreparedStatement.setDate(i, val)...).
   *
   * Grâce à Spring Data JPA (et JPQL/HQL), nous pouvons écrire une clause
   * statique : "(:param IS NULL OR t.field = :param)"
   * Hibernate s'occupe de compiler intelligemment la requête et de sécuriser
   * l'injection SQL automatiquement. Le code reste concis et sécurisé."""
content = content.replace(old_comment2, new_comment2)

# Fix long method signature countByAssignedTechnicianEmailAndStatus
content = content.replace("""  long countByAssignedTechnicianEmailAndStatus(String email, TicketStatus status);""",
                          """  long countByAssignedTechnicianEmailAndStatus(
      String email,
      TicketStatus status);""")

# Fix long method signature countByAssignedTechnicianEmailAndPriorityAndStatusIn
content = content.replace("""  long countByAssignedTechnicianEmailAndPriorityAndStatusIn(
      String email,
      Priority priority,
      List<TicketStatus> statuses);""",
                          """  long countByAssignedTechnicianEmailAndPriorityAndStatusIn(
      String email,
      Priority priority,
      List<TicketStatus> statuses);""")

# For line 191
content = content.replace("""  long countByAssignedTechnicianEmailAndPriorityAndStatusIn(String email, TicketStatus status);""",
"""  long countByAssignedTechnicianEmailAndPriorityAndStatusIn(
      String email,
      TicketStatus status);""")

with open(filepath, 'w') as f:
    f.write(content)
