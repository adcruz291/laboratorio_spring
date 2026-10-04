package com.miagenda.web;

import com.miagenda.auth.SecurityService;
import com.miagenda.model.User;
import com.miagenda.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthApiController {

    public record UserCreate(@NotBlank String username, @NotBlank String password) {}
    public record UserRead(Long id, String username) {}
    public record Token(String access_token, String token_type) {}

    private final UserRepository users;
    private final SecurityService security;

    public AuthApiController(UserRepository users, SecurityService security) {
        this.users = users;
        this.security = security;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRead register(@RequestBody @jakarta.validation.Valid UserCreate data) {
        if (users.findByUsername(data.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe");
        }
        User user = users.save(new User(data.username(), security.hashPassword(data.password())));
        return new UserRead(user.getId(), user.getUsername());
    }

    /** Equivale a OAuth2PasswordRequestForm: form-urlencoded con username y password. */
    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        User user = users.findByUsername(username).orElse(null);
        if (user == null || !security.verifyPassword(password, user.getHashedPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                    .body(new ApiError("Usuario o contraseña incorrectos"));
        }
        return ResponseEntity.ok(new Token(security.createAccessToken(user.getUsername()), "bearer"));
    }
}
