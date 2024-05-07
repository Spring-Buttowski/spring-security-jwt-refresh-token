package com.alexandrov.springsecurityjwtrefreshtoken.controller;

import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AuthenticationRequest;
import com.alexandrov.springsecurityjwtrefreshtoken.model.dto.AccessAndRefreshToken;
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
     * @param authenticationRequest The customer's email and password.
     * @return access and refresh tokens with HTTPS status code {@code 200} if the authentication is successful.
     * {@code 401} in case credentials are wrong, 400 when client is trying to authenticate second time.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        AccessAndRefreshToken accessAndRefreshToken = authenticationService.authenticate(authenticationRequest);
        return ResponseEntity.ok(accessAndRefreshToken);
    }

    /**
     * Get new access and refresh token.
     * If a refreshToken is not valid, will return 400 HTTP status code.
     *
     * @param request HttpServletRequest with 'Authorization' header having refresh-token on board.
     * @return refresh-token and access-token with HTTP status code {200} if the data is valid, {@code 400} otherwise.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> getNewTokens(HttpServletRequest request) {
        AccessAndRefreshToken accessAndRefreshToken = authenticationService.receiveNewTokens(request);
        return ResponseEntity.ok(accessAndRefreshToken);
    }

    /**
     * Execute a logout on the particular device.
     * If a refreshToken is not valid, will return 400 HTTP status code.
     *
     * @param request HttpServletRequest with 'Authorization' header having refresh-token on board.
     * @return {200} if the logout is successful, {@code 400} otherwise.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok("You have been logged out");
    }

}