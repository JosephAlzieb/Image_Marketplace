package com.marketplace.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.UserRole;
import com.marketplace.model.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String email, String password, String firstName, String lastName,
                         UserRole role, UserStatus status, Boolean emailVerified, LocalDateTime lastLoginAt,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.emailVerified = emailVerified;
        this.lastLoginAt = lastLoginAt;
        this.authorities = authorities;
    }

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = getAuthorities(user.getRole());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getEmailVerified(),
                user.getLastLoginAt(),
                authorities
        );
    }

    /**
     * Get authorities based on user role
     */
    private static List<GrantedAuthority> getAuthorities(UserRole role) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authorities
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // Add hierarchical authorities
        switch (role) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM_SELLER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
                break;
            case PREMIUM_SELLER:
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM_SELLER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
                break;
            case SELLER:
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
                break;
            case BUYER:
                authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
                break;
        }

        // Add permission-based authorities
        authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_PUBLIC"));

        if (role == UserRole.SELLER || role == UserRole.PREMIUM_SELLER || role == UserRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_UPLOAD_IMAGE"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_SELL"));
        }

        if (role == UserRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_ADMIN_ALL"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MODERATE"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_USERS"));
        }

        return authorities;
    }

    // UserDetails interface methods

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    // Helper methods

    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (this.role == role) return true;
        }
        return false;
    }

    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isSeller() {
        return role == UserRole.SELLER || role == UserRole.PREMIUM_SELLER || role == UserRole.ADMIN;
    }

    public boolean isPremiumSeller() {
        return role == UserRole.PREMIUM_SELLER || role == UserRole.ADMIN;
    }

    // equals and hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}