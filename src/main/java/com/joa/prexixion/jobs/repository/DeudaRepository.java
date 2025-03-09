package com.joa.prexixion.jobs.repository;

import com.joa.prexixion.jobs.model.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeudaRepository extends CrudRepository<Deuda, Integer> {

    @Modifying
    @Query("UPDATE Deuda d SET d.idEstado = 3 " +
            "WHERE idCliente = :idCliente " +
            "AND idEstadoPago = 1 " +
            "AND idEstado NOT IN (3, 5) " +
            "AND d.fVencimiento IS NOT NULL")
    int marcarDeudasComoCoactivas(@Param("idCliente") String idCliente,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaActual") LocalDate fechaActual);

    @Query("SELECT d FROM Deuda d " +
            "WHERE d.idCliente = :idCliente " +
            "AND d.idEstadoPago = 1 " +
            "AND d.idEstado NOT IN (3, 5)")
    List<Deuda> obtenerDeudas(@Param("idCliente") String idCliente);
}
