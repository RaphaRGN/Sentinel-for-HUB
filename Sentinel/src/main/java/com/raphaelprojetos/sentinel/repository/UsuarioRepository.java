package com.raphaelprojetos.sentinel.repository;

import com.raphaelprojetos.sentinel.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository <Usuario, Long> {
}
