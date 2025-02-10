package com.raphaelprojetos.sentinel.repository;

import com.raphaelprojetos.sentinel.entities.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaRepository extends JpaRepository <Alerta, Long> {
}
