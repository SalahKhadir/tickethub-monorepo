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
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users whose accounts are disabled (enabled = false).
     *
     * @return a list of pending/disabled users
     */
    List<User> findAllByEnabledFalse();

    /**
     * Finds all enabled users having a specific role.
     *
     * @param role the role to filter by
     * @return a list of matching enabled users
     */
    @Query("""
           SELECT DISTINCT u
           FROM User u
           JOIN u.roles r
           WHERE r = :role
             AND u.enabled = true
           """)
    List<User> findByRole(@Param("role") Role role);
}
