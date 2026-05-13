package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.services;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null && username.contains("@")) {
            return loadUserByEmail(username);
        }
        UUID id;
        try {
            id = UUID.fromString(username);
        } catch (IllegalArgumentException exception) {
            return loadUserByEmail(username);
        }
        var userId = new UserId(id);
        var user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));
        return UserDetailsImpl.build(user);
    }

    private UserDetails loadUserByEmail(String email) {
        var user = userRepository.findByEmailWithRoles(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDetailsImpl.build(user);
    }
}
