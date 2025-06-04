package com.joa.prexixion.jobs.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.joa.prexixion.jobs.model.Deuda;
import com.joa.prexixion.jobs.model.XentraReporte;

@Repository
public interface XentraReporteRepository extends CrudRepository<XentraReporte, Integer> {

    //@Modifying
    //@Transactional
    //@Query(value = "UPDATE xentraFechas SET estadoLogico = 'BLOQUEADO' " +
    //        "WHERE idEstado = 1 AND id = :id AND fecha < :fecha", nativeQuery = true)
    //String marcarXentraReporteComoBloqueado(@Param("id") int id, @Param("fecha") LocalDate fecha);

    @Query(value = "SELECT * FROM xentraFechas WHERE idEstado = 1 " +
            "AND estadoLogico = 'PENDIENTE' AND fecha <= :fechaLimite", nativeQuery = true)
    List<XentraReporte> obtenerXentraReporteParaBloqueo(@Param("fechaLimite") LocalDate fechaLimite);
}
