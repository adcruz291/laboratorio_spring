package com.miagenda.auth;

import com.miagenda.model.User;
import com.miagenda.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserResolver {

    private final SecurityService security;
    private final UserRepository users;

    public CurrentUserResolver(SecurityService security, UserRepository users) {
        this.security = security;
        this.users = users;
    }

    /** Usuario a partir del header Authorization: Bearer (API). */
    public Optional<User> fromBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        return fromToken(header.substring(7).trim());
    }

    /** Usuario a partir de la cookie access_token (vistas). */
    public Optional<User> fromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie c : request.getCookies()) {
            if ("access_token".equals(c.getName())) {
                return fromToken(c.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<User> fromToken(String token) {
        return security.decodeToken(token).flatMap(users::findByUsername);
    }
}
