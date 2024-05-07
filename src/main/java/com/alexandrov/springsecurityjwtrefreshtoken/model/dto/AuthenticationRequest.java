package com.alexandrov.springsecurityjwtrefreshtoken.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
