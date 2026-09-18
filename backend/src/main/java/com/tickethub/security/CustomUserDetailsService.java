package com.tickethub.security;

import com.tickethub.model.User;
import com.tickethub.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public final class CustomUserDetailsService implements UserDetailsService {
    /**
     * Javadoc.
     */
    private final UserRepository userRepository;

    /**
     * Creates a user details service.
     *
     * @param pUserRepository repository for user operations
     */
    public CustomUserDetailsService(final UserRepository pUserRepository) {
        this.userRepository = pUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            final String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities);
    }
}
