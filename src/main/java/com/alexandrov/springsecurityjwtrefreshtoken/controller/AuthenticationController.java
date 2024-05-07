package com.alexandrov.springsecurityjwtrefreshtoken.controller;

import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AuthenticationRequest;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AuthenticationResponse;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.SingUpRequest;
import com.alexandrov.springsecurityjwtrefreshtoken.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new customer. If a customer with this email already exists, will return 409 HTTP status code.
     *
     * @param singUpRequest The new customer's data.
     * @return {@code 200} if the customer was registered, {@code 409} otherwise.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid SingUpRequest singUpRequest) {
        authenticationService.register(singUpRequest);
        return ResponseEntity.ok("User has been registered!");
    }

    /**
     * Authenticate a customer. If authentication is unsuccessful, will return 4** HTTP status code.
     *
     * @param authenticationRequest The new customer's email and password.
     * @return access and refresh tokens and session data with HTTPS status code {@code 200} if the authentication is successful.
     * {@code 401} in case credentials are wrong, 400 when client is trying to authenticate second time.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);
        return ResponseEntity.ok(authenticationResponse);
    }

    /**
     * Get a new access-token of a customer.
     * If a sessionData, whether a refreshToken is not valid, will return 400 HTTP status code.
     *
     * @param request The customer's refreshToken and session.
     * @return access-token with HTTPS status code {@code 200} if the data is valid, {@code 400} otherwise.
     */
    @PostMapping("/access-token")
    public ResponseEntity<?> getNewAccessToken(HttpServletRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.receiveNewAccessToken(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    /**
     * Get a new refresh-token of a customer.
     * If a sessionData, whether a refreshToken is not valid, will return 400 HTTP status code.
     *
     * @param request The customer's refreshToken and session.
     * @return refresh-token with HTTP status code {200} if the data is valid, {@code 400} otherwise.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> getNewRefreshToken(HttpServletRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.receiveNewRefreshToken(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    /**
     * Execute a logout on the particular device.
     * If a sessionData, whether a refreshToken is not valid, will return 400 HTTP status code.
     *
     * @param request The customer's refreshToken and session.
     * @return {200} if the logout is successful, {@code 400} otherwise.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok("You have been logged out");
    }

}