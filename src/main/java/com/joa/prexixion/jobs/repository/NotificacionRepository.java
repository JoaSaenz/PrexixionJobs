package com.joa.prexixion.jobs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    boolean existsByRucAndIdSunat(String ruc, String idSunat);

    @Query("SELECT n.idSunat FROM Notificacion n WHERE n.ruc = :ruc AND n.idSunat IN :ids")
List<String> findExistentes(@Param("ruc") String ruc, @Param("ids") List<String> ids);

}
