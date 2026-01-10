package com.joa.prexixion.jobs.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.joa.prexixion.jobs.model.XentraReporte;

@Repository
public interface XentraReporteRepository extends CrudRepository<XentraReporte, Integer> {

    @Query(value = "SELECT * FROM xentraFechas WHERE idEstado = 1 " +
            "AND estadoLogico = 'PENDIENTE' AND fecha <= :fechaLimite", nativeQuery = true)
    List<XentraReporte> obtenerXentraReporteParaBloqueo(@Param("fechaLimite") LocalDate fechaLimite);
}
