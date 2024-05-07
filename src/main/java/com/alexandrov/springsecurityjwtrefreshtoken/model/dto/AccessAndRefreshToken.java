package com.alexandrov.springsecurityjwtrefreshtoken.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessAndRefreshToken {
    private String accessToken;
    private String refreshToken;
}
