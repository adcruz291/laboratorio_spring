package com.miagenda.auth;

import com.miagenda.config.AppProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Hash de contraseñas (bcrypt) y tokens JWT HS256. */
@Component
public class SecurityService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecretKey key;
    private final Duration expiration;

    public SecurityService(AppProperties props) {
        // HS256 exige >= 32 bytes; PyJWT no lo valida, jjwt sí.
        this.key = Keys.hmacShaKeyFor(props.secretKey().getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(props.accessTokenExpireMinutes());
    }

    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    public boolean verifyPassword(String password, String hashed) {
        return encoder.matches(password, hashed);
    }

    public String createAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .expiration(Date.from(Instant.now().plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<String> decodeToken(String token) {
        try {
            return Optional.ofNullable(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
