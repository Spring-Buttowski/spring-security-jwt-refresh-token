package com.alexandrov.springsecurityjwtrefreshtoken.service;

import com.alexandrov.springsecurityjwtrefreshtoken.constants.ProjectConstants;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AuthenticationRequest;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AccessAndRefreshToken;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.SingUpRequest;
import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.User;
import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.UserSession;
import com.alexandrov.springsecurityjwtrefreshtoken.repositories.UserRepository;
import com.alexandrov.springsecurityjwtrefreshtoken.repositories.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager,
                                 PasswordEncoder passwordEncoder, JwtService jwtService,
                                 UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
    }

    //    @Transactional
    public AccessAndRefreshToken authenticate(AuthenticationRequest authenticationRequest) {
        //Check whether credentials are authentic
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        //If authentic, send access and refresh tokens
        if (null != authentication && authentication.isAuthenticated()) {
            //Create new access and refresh tokens
            User user = userRepository.findByEmail(authenticationRequest.getEmail()).get();
            //If user has already been authenticated
            if (userSessionRepository.findUserSessionByUser(user).isPresent()) {
                throw new IllegalStateException("User has already been authenticated!");
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            //Save a new user session
            userSessionRepository.save(UserSession
                    .builder()
                    .user(user)
                            .expiresAt(LocalDateTime.now().plusDays(ProjectConstants.REFRESH_TOKEN_EXPIRATION_IN_DAYS))
                    .refreshToken(refreshToken)
                    .build());

            return AccessAndRefreshToken
                    .builder()
                    .refreshToken(refreshToken)
                    .accessToken(accessToken)
                    .build();
        } else {
            throw new BadCredentialsException("Wrong credentials!");
        }
    }

    public void register(SingUpRequest singUpRequest) {
        if (userRepository.findByEmail(singUpRequest.getEmail()).isPresent()) {
            throw new EntityExistsException("User with this email already exists!");
        }
        //Create and save a new customer
        userRepository.save(User.builder()
                .email(singUpRequest.getEmail())
                .password(passwordEncoder.encode(singUpRequest.getPassword()))
                .name(singUpRequest.getName())
                .role(ProjectConstants.USER_ROLE)
                .build());
    }

    @Transactional
    public AccessAndRefreshToken receiveNewTokens(HttpServletRequest request) {
        //Take a refresh token out from headers
        String refreshToken = getToken(request);

        //Check whether a refresh token is authentic and not expired
        if (jwtService.validateRefreshToken(refreshToken)) {
            //We just knew that a refresh token is valid. Let's see if we have session with this user in our DB.
            Claims claims = jwtService.getRefreshClaims(refreshToken);

            //Take user's id out of token
            String id = claims.getSubject();

            //Find user in our DB by id
            Optional<User> optionalUser = userRepository.findById(Integer.valueOf(id));

            //Find user's session
            Optional<UserSession> optionalUserSession = userSessionRepository.findUserSessionByUser(optionalUser.orElseThrow(NoSuchElementException::new));

            //Check that user exists and his session's refresh token is the same as we received
            if (optionalUserSession.isPresent() && optionalUserSession.get().getRefreshToken().equals(refreshToken)) {
                //Create new accessToken and refreshToken and respond
                String newRefreshToken = jwtService.generateRefreshToken(optionalUser.get());
                String newAccessToken = jwtService.generateAccessToken(optionalUser.get());
                optionalUserSession.get().setRefreshToken(newRefreshToken);
                return AccessAndRefreshToken
                        .builder()
                        .refreshToken(newRefreshToken)
                        .accessToken(newAccessToken)
                        .build();
            } else {
                throw new JwtException("Invalid token!");
            }
        }
        return AccessAndRefreshToken
                .builder()
                .refreshToken(null)
                .accessToken(null)
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        //If user has already logged out
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new IllegalStateException("User has already been logged out!");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        String token = getToken(request);
        userSessionRepository.deleteByRefreshToken(token);
    }


    private String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}
