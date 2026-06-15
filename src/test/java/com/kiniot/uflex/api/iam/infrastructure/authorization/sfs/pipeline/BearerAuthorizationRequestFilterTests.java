package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline;

import com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.kiniot.uflex.api.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BearerAuthorizationRequestFilterTests {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validJwtCreatesAuthenticationWithTenantAwarePrincipal() throws Exception {
        var tokenService = mock(BearerTokenService.class);
        var userDetailsService = mock(UserDetailsService.class);
        var filter = new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);
        var principal = new UserDetailsImpl(
                "user-123",
                "hashed-password",
                "patient@example.com",
                "clinic-456",
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        when(tokenService.getBearerTokenFrom(request)).thenReturn("valid-token");
        when(tokenService.validateToken("valid-token")).thenReturn(true);
        when(tokenService.getUsernameFromToken("valid-token")).thenReturn("user-123");
        when(userDetailsService.loadUserByUsername("user-123")).thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertSame(principal, authentication.getPrincipal());
        assertEquals("clinic-456", ((UserDetailsImpl) authentication.getPrincipal()).getTenant());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidJwtLeavesRequestUnauthenticated() throws Exception {
        var tokenService = mock(BearerTokenService.class);
        var userDetailsService = mock(UserDetailsService.class);
        var filter = new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = mock(jakarta.servlet.FilterChain.class);

        when(tokenService.getBearerTokenFrom(request)).thenReturn("invalid-token");
        when(tokenService.validateToken("invalid-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }
}
