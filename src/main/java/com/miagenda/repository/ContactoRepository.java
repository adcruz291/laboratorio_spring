package com.miagenda.repository;

import com.miagenda.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    List<Contacto> findByUserId(Long userId);
    Optional<Contacto> findByIdAndUserId(Long id, Long userId);
}
