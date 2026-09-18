import re

filepath = "src/main/java/com/tickethub/service/UserService.java"
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
"""        // Admin creates a default tech or client. Assuming Tech for default, but can be improved.
        // Let's assign ROLE_TECH by default or adjust as needed. We'll set ROLE_TECH for now.""",
"""        // Admin creates a default technician or client.
        // The current default is ROLE_TECH.""")

content = content.replace(
"""                    int w1 = r1.equals("ROLE_ADMIN") ? ADMIN_ROLE_WEIGHT : r1.
                        equals(
                                "ROLE_TECH") ? TECH_ROLE_WEIGHT : CLIENT_ROLE_WEIGHT;
                    int w2 = r2.equals("ROLE_ADMIN") ? ADMIN_ROLE_WEIGHT : r2.
                        equals(
                                "ROLE_TECH") ? TECH_ROLE_WEIGHT : CLIENT_ROLE_WEIGHT;""",
"""                    int w1 = r1.equals("ROLE_ADMIN")
                            ? ADMIN_ROLE_WEIGHT
                            : r1.equals("ROLE_TECH")
                                    ? TECH_ROLE_WEIGHT
                                    : CLIENT_ROLE_WEIGHT;
                    int w2 = r2.equals("ROLE_ADMIN")
                            ? ADMIN_ROLE_WEIGHT
                            : r2.equals("ROLE_TECH")
                                    ? TECH_ROLE_WEIGHT
                                    : CLIENT_ROLE_WEIGHT;""")

content = content.replace(
"""     * En JDBC classique, pour récupérer les techniciens (utilisateurs ayant le
         rôle TECHNICIAN),
     * il aurait fallu écrire une requête native complexe avec une jointure
         explicite,
     * par exemple :
     * SELECT u.* FROM users u INNER JOIN user_roles ur ON u.id = ur.user_id
         WHERE ur.role = 'ROLE_TECH';
     * Il aurait ensuite fallu mapper manuellement le ResultSet vers l'objet
         User.
     *
     * Avec Spring Data JPA, tout cela est géré automatiquement.
     * Le repository 'userRepository.findByRole(Role.ROLE_TECH)' (ou une simple
         @Query JPQL)
     * s'occupe de la jointure derrière les coulisses grâce au mapping ORM (
         comme @ElementCollection ou les relations ManyToMany).""",
"""     * En JDBC classique, pour récupérer les techniciens (utilisateurs ayant le
     * rôle TECHNICIAN), il aurait fallu écrire une requête native complexe avec
     * une jointure explicite, par exemple :
     * SELECT u.* FROM users u INNER JOIN user_roles ur ON u.id = ur.user_id
     * WHERE ur.role = 'ROLE_TECH';
     * Il aurait ensuite fallu mapper manuellement le ResultSet vers l'objet
     * User.
     *
     * Avec Spring Data JPA, tout cela est géré automatiquement.
     * Le repository 'userRepository.findByRole(Role.ROLE_TECH)' (ou une simple
     * @Query JPQL) s'occupe de la jointure derrière les coulisses grâce au
     * mapping ORM (comme @ElementCollection ou les relations ManyToMany).""")

content = content.replace(
"""     * En J2EE classique, tu aurais dû gérer manuellement les états de compte
         dans
     * la session (HttpSession) ou via des filtres Servlet complexes pour
         vérifier
     * à chaque requête si l'utilisateur est approuvé.
     *
     * Alors que Spring Security intègre nativement la gestion du statut
         'enabled'
     * dans l'interface UserDetails. Si l'attribut boolean 'enabled' est false,
     * Spring Security bloquera la génération du JWT ou l'authentification avec
         un
     * DisabledException de manière totalement transparente.""",
"""     * En J2EE classique, tu aurais dû gérer manuellement les états de compte
     * dans la session (HttpSession) ou via des filtres Servlet complexes pour
     * vérifier à chaque requête si l'utilisateur est approuvé.
     *
     * Alors que Spring Security intègre nativement la gestion du statut
     * 'enabled' dans l'interface UserDetails. Si l'attribut boolean 'enabled'
     * est false, Spring Security bloquera la génération du JWT ou
     * l'authentification avec un DisabledException de manière totalement
     * transparente.""")

content = content.replace(
"""     * Creates a new user account with default roles (e.g., TECH) for
         administrative purposes.""",
"""     * Creates a new user account with default roles (e.g., TECH) for
     * administrative purposes.""")

with open(filepath, 'w') as f:
    f.write(content)
