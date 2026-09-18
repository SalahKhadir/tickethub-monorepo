package com.tickethub.repository;

import com.tickethub.model.Role;
import com.tickethub.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Javadoc.
      * @return description
      * @param email description
     */
    Optional<User> findByEmail(String email);

    /**
     * Javadoc.
      * @return description
      * @param email description
     */
    boolean existsByEmail(String email);

    /**
     * Javadoc.
      * @return description
     */
    List<User> findAllByEnabledFalse();

    /**
     * Javadoc.
      * @param role description
      * @return description
     */
    @Query("select distinct u from User u join u.roles r where r = :role and u.enabled = true")
    List<User> findByRole(@Param("role") Role role);
}
