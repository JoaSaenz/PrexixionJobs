package com.joa.prexixion.jobs.repository;

import com.joa.prexixion.jobs.model.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DeudaRepository extends CrudRepository<Deuda, Integer> {

        @Modifying
        @Transactional
        @Query(value = "UPDATE DeudasNew SET idEstado = 3 " +
                        "WHERE idCliente = :idCliente " +
                        "AND idEstadoPago = 1 " +
                        "AND idEstado NOT IN (3, 5) " +
                        "AND fVencimiento > :fechaInicio " +
                        "AND fVencimiento < :fechaActual " +
                        "AND fVencimiento IS NOT NULL", nativeQuery = true)
        int marcarDeudasComoCoactivas(@Param("idCliente") String idCliente,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaActual") LocalDate fechaActual);

        @Query(value = "SELECT * FROM DeudasNew WHERE idCliente = :idCliente " +
                        "AND idEstadoPago = 1 " +
                        "AND idEstado NOT IN (3, 5)", nativeQuery = true)
        List<Deuda> obtenerDeudas(@Param("idCliente") String idCliente);
}
