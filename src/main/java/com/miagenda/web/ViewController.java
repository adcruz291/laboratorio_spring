package com.miagenda.web;

import com.miagenda.auth.CurrentUserResolver;
import com.miagenda.auth.SecurityService;
import com.miagenda.model.Contacto;
import com.miagenda.model.User;
import com.miagenda.repository.ContactoRepository;
import com.miagenda.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import java.util.Optional;

@Controller
public class ViewController {

    private final UserRepository users;
    private final ContactoRepository contactos;
    private final SecurityService security;
    private final CurrentUserResolver currentUser;

    public ViewController(UserRepository users, ContactoRepository contactos,
                          SecurityService security, CurrentUserResolver currentUser) {
        this.users = users;
        this.contactos = contactos;
        this.security = security;
        this.currentUser = currentUser;
    }

    private String seeOther(HttpServletRequest request, String url) {
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.SEE_OTHER);
        return "redirect:" + url;
    }

    private String loginRedirect(String username, HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", security.createAccessToken(username))
                .httpOnly(true).sameSite("Lax").path("/").build();
        response.addHeader("Set-Cookie", cookie.toString());
        return seeOther(request, "/agenda");
    }

    private String loginError(String error, HttpStatus status, Model model, HttpServletResponse response) {
        model.addAttribute("error", error);
        response.setStatus(status.value());
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/agenda";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("error", null);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletRequest request, Model model, HttpServletResponse response) {
        Optional<User> user = users.findByUsername(username);
        if (user.isEmpty() || !security.verifyPassword(password, user.get().getHashedPassword())) {
            return loginError("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED, model, response);
        }
        return loginRedirect(user.get().getUsername(), request, response);
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String username, @RequestParam String password,
                           HttpServletRequest request, Model model, HttpServletResponse response) {
        if (users.findByUsername(username).isPresent()) {
            return loginError("El usuario ya existe", HttpStatus.BAD_REQUEST, model, response);
        }
        User user = users.save(new User(username, security.hashPassword(password)));
        return loginRedirect(user.getUsername(), request, response);
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie expired = ResponseCookie.from("access_token", "").path("/").maxAge(0).build();
        response.addHeader("Set-Cookie", expired.toString());
        return seeOther(request, "/login");
    }

    @GetMapping("/agenda")
    public String agenda(HttpServletRequest request, Model model) {
        Optional<User> user = currentUser.fromCookie(request);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("user", user.get());
        model.addAttribute("contactos", contactos.findByUserId(user.get().getId()));
        return "agenda";
    }

    @PostMapping("/agenda")
    public String agregar(HttpServletRequest request, @RequestParam String nombre,
                          @RequestParam String telefono, @RequestParam(defaultValue = "") String email) {
        Optional<User> user = currentUser.fromCookie(request);
        if (user.isEmpty()) {
            return seeOther(request, "/login");
        }
        contactos.save(new Contacto(nombre, telefono, email.isEmpty() ? null : email, user.get().getId()));
        return seeOther(request, "/agenda");
    }
}
