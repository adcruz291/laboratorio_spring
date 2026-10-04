package com.miagenda.web;

import com.miagenda.auth.CurrentUserResolver;
import com.miagenda.model.Contacto;
import com.miagenda.model.User;
import com.miagenda.repository.ContactoRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/contactos")
public class ContactoApiController {

    public record ContactoCreate(@NotBlank String nombre, @NotBlank String telefono, String email) {}
    public record ContactoUpdate(String nombre, String telefono, String email) {}
    public record ContactoRead(Long id, String nombre, String telefono, String email, Long user_id) {
        static ContactoRead of(Contacto c) {
            return new ContactoRead(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail(), c.getUserId());
        }
    }

    private final ContactoRepository contactos;
    private final CurrentUserResolver currentUser;

    public ContactoApiController(ContactoRepository contactos, CurrentUserResolver currentUser) {
        this.contactos = contactos;
        this.currentUser = currentUser;
    }

    private User user(HttpServletRequest request) {
        return currentUser.fromBearer(request).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
    }

    private Contacto own(Long id, User user) {
        return contactos.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contacto no encontrado"));
    }

    @GetMapping({"", "/"})
    public List<ContactoRead> listar(HttpServletRequest request) {
        return contactos.findByUserId(user(request).getId()).stream().map(ContactoRead::of).toList();
    }

    @GetMapping("/{id}")
    public ContactoRead obtener(@PathVariable Long id, HttpServletRequest request) {
        return ContactoRead.of(own(id, user(request)));
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public ContactoRead crear(@RequestBody @Valid ContactoCreate data, HttpServletRequest request) {
        User user = user(request);
        return ContactoRead.of(contactos.save(
                new Contacto(data.nombre(), data.telefono(), data.email(), user.getId())));
    }

    /** Actualización parcial: solo se modifican los campos enviados. */
    @PutMapping("/{id}")
    public ContactoRead actualizar(@PathVariable Long id, @RequestBody ContactoUpdate data,
                                   HttpServletRequest request) {
        Contacto c = own(id, user(request));
        if (data.nombre() != null) c.setNombre(data.nombre());
        if (data.telefono() != null) c.setTelefono(data.telefono());
        if (data.email() != null) c.setEmail(data.email());
        return ContactoRead.of(contactos.save(c));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, HttpServletRequest request) {
        contactos.delete(own(id, user(request)));
    }
}
