package com.joa.prexixion.jobs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.GestionArrendamientoContrato;

@Repository
public interface GestionArrendamientoContratoRepository extends JpaRepository<GestionArrendamientoContrato, Integer> {

    /**
     * Obtiene todos los contratos que tengan estado VIGENTE o POR VENCER
     * 
     * @param estados Lista de estados a filtrar
     * @return Lista de contratos
     */
    List<GestionArrendamientoContrato> findByEstadoLogicoIn(List<String> estados);
}
