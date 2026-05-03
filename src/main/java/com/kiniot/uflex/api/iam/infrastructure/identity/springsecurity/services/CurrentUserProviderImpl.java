package com.kiniot.uflex.api.iam.infrastructure.identity.springsecurity.services;

import com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.kiniot.uflex.api.iam.infrastructure.identity.springsecurity.SpringSecurityCurrentUserProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CurrentUserProviderImpl implements SpringSecurityCurrentUserProvider {

    @Override
    public Optional<String> getUserId() {
        return getPrincipal().map(UserDetailsImpl::getUsername);
    }

    @Override
    public Optional<String> getEmail() {
        return getPrincipal().map(UserDetailsImpl::getEmail);
    }

    @Override
    public Set<String> getRoles() {
        return SecurityContextHolder.getContext()
                .getAuthentication() != null
                ? SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                : Set.of();
    }

    @Override
    public Optional<String> getTenantId() {
        return getPrincipal().map(UserDetailsImpl::getTenant);
    }

    @Override
    public boolean isServiceAccount() {
        /* This is a placeholder implementation. You should replace this with actual logic to determine if the user is a service account. */
        return false;
    }

    private Optional<UserDetailsImpl> getPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }
}