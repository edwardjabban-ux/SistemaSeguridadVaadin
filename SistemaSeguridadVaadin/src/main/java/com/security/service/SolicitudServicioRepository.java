package com.security.service;

import com.security.model.SolicitudServicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicio, Long> {
}
