package com.joa.prexixion.jobs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    boolean existsByRucAndIdSunat(String ruc, String idSunat);
}
