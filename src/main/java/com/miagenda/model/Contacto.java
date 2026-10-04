package com.miagenda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contacto", indexes = @Index(columnList = "user_id"))
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String telefono;

    private String email;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    protected Contacto() {
    }

    public Contacto(String nombre, String telefono, String email, Long userId) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getUserId() { return userId; }
}
