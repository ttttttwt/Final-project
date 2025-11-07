package com.lexia.backend.auth;

import com.lexia.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Custom {@link UserDetails} implementation that retains the authenticated
 * {@link User} entity while exposing Spring Security credentials. This allows
 * downstream layers (controllers, services) to access the full domain user
 * without additional database lookups when using
 * {@code @AuthenticationPrincipal}.
 */
public class AuthenticatedUserDetails implements UserDetails {

    private final User user;
    private final Set<? extends GrantedAuthority> authorities;

    public AuthenticatedUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = Objects.requireNonNull(user, "User must not be null");
        if (authorities == null || authorities.isEmpty()) {
            this.authorities = Collections.emptySet();
        } else {
            this.authorities = Collections.unmodifiableSet(new HashSet<>(authorities));
        }
    }

    /**
     * Exposes the authenticated {@link User} entity for application use.
     *
     * @return the authenticated user
     */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(user.getIsActive());
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(user.getIsActive());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(user.getIsActive());
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getIsActive());
    }
}
