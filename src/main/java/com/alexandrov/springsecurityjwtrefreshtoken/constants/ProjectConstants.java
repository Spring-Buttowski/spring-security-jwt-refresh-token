package com.alexandrov.springsecurityjwtrefreshtoken.constants;

import org.springframework.security.core.context.SecurityContextHolder;

public interface ProjectConstants {
    int ACCESS_TOKEN_EXPIRATION_IN_MINUTES = 3;
    int REFRESH_TOKEN_EXPIRATION_IN_DAYS = 30;
    String ROLE = "role";
    String USER_ROLE = "ROLE_USER";
    String JWT_HEADER = "Authorization";
    String JWT_ISSUER = "backend";

    String[][] WHITE_LIST = {
            //MvcRequestMatchers
            {"/auth/**", "/products"},
            //Java regex matchers
            {"^/auth/.*", "^/products"}
    };

}
