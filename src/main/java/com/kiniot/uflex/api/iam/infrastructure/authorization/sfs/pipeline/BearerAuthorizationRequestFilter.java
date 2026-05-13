package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline;

import com.kiniot.uflex.api.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;

    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            LOGGER.info("Authorization header present: {}", StringUtils.hasText(request.getHeader("Authorization")));
            LOGGER.info("Bearer token received: {}", maskToken(token));
            if (token != null && tokenService.validateToken(token)) {
                String subject = tokenService.getSubjectFromToken(token);
                String email = tokenService.getEmailFromToken(token);
                var roles = tokenService.getRolesFromToken(token);
                LOGGER.info("JWT subject extracted: {}", subject);
                LOGGER.info("JWT email extracted: {}", email);
                LOGGER.info("JWT roles extracted: {}", roles);
                var userDetails = loadUserDetails(subject, email);
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                LOGGER.info("User authorities: {}", userDetails.getAuthorities());
                LOGGER.info("SecurityContext authenticated: {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                LOGGER.info("Token is not valid");
            }

        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private UserDetails loadUserDetails(String subject, String email) {
        if (StringUtils.hasText(email)) {
            try {
                var userDetails = userDetailsService.loadUserByUsername(email);
                LOGGER.info("User found by JWT email: {}", email);
                return userDetails;
            } catch (UsernameNotFoundException exception) {
                LOGGER.info("User not found by JWT email: {}", email);
                throw exception;
            }
        }
        try {
            var userDetails = userDetailsService.loadUserByUsername(subject);
            LOGGER.info("User found by JWT subject: {}", subject);
            return userDetails;
        } catch (UsernameNotFoundException exception) {
            LOGGER.info("User not found by JWT subject: {}", subject);
            throw exception;
        }
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token)) return null;
        if (token.length() <= 12) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }
}
