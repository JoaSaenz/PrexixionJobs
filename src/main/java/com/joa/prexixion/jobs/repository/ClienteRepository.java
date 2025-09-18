package com.joa.prexixion.jobs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    @Query(value = "SELECT ruc, solU, solC FROM cliente WHERE idEstado = :idEstado and y = :y", nativeQuery = true)
    List<Cliente> obtenerClientesActivos(@Param("idEstado") Integer idEstado,
            @Param("y") Integer y);
}
