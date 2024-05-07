package com.alexandrov.springsecurityjwtrefreshtoken.security;

import com.alexandrov.springsecurityjwtrefreshtoken.constants.ProjectConstants;
import com.alexandrov.springsecurityjwtrefreshtoken.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class AccessTokenValidatorFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = request.getHeader(ProjectConstants.JWT_HEADER);
        if (null != jwt) {
            try {
                jwt = jwt.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(JwtService.ACCESS_TOKEN_SECRET_KEY)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String id = claims.getSubject();
                String role = String.valueOf(claims.get(ProjectConstants.ROLE));
                Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

                Authentication auth = new UsernamePasswordAuthenticationToken(id, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException expEx) {
                log.error("Token expired", expEx);
            } catch (UnsupportedJwtException unsEx) {
                log.error("Unsupported jwt", unsEx);
            } catch (MalformedJwtException mjEx) {
                log.error("Malformed jwt", mjEx);
            } catch (SignatureException sEx) {
                log.error("Invalid signature", sEx);
            } catch (Exception e) {
                log.error("invalid token", e);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Arrays.stream(ProjectConstants.WHITE_LIST[1]).anyMatch(str -> request.getServletPath().matches(str));
    }

}
