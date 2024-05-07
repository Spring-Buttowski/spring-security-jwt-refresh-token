package com.alexandrov.springsecurityjwtrefreshtoken.security;

import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.User;
import com.alexandrov.springsecurityjwtrefreshtoken.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsernamePasswordAuthenticationProvider(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        Optional<User> optionalCustomer = userRepository.findByEmail(email);
        if (optionalCustomer.isPresent()) {
            if (passwordEncoder.matches(password, optionalCustomer.get().getPassword())) {
                return new UsernamePasswordAuthenticationToken(email, password,
                        Collections.singletonList(new SimpleGrantedAuthority(optionalCustomer.get().getRole())));
            } else {
                throw new BadCredentialsException("Wrong credentials!");
            }
        } else {
            throw new BadCredentialsException("Wrong credentials!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
