package com.gpustore.security;

import com.gpustore.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal representing an authenticated user.
 *
 * <p>Implements {@link UserDetails} to integrate with Spring Security's
 * authentication framework. Wraps user information including ID, email,
 * password hash, and granted authorities.</p>
 *
 * <p>All accounts are treated as non-expired, non-locked, and enabled.</p>
 *
 * @author GPU Store Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new UserPrincipal with the specified user details.
     *
     * @param id          the user's unique identifier
     * @param email       the user's email address (used as username)
     * @param password    the user's hashed password
     * @param authorities the granted authorities for the user
     */
    public UserPrincipal(Long id, String email, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method to create a UserPrincipal from a User entity.
     *
     * <p>Assigns the default {@code ROLE_USER} authority to all users.</p>
     *
     * @param user the user entity to convert
     * @return a new UserPrincipal instance
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * Returns the user's unique identifier.
     *
     * @return the user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** {@inheritDoc} */
    @Override
    public String getPassword() {
        return password;
    }

    /** {@inheritDoc} */
    @Override
    public String getUsername() {
        return email;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
