package com.joa.prexixion.jobs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    @Query(value = "SELECT c.ruc, c.solU, c.solC FROM cliente c LEFT JOIN signerNiveles n ON c.ruc = n.idCliente WHERE c.idEstado = :idEstado and c.y = :y and n.idNivelF = 4 and n.idNivelX3 = 5", nativeQuery = true)
    List<Cliente> obtenerClientesActivos(@Param("idEstado") Integer idEstado,
            @Param("y") Integer y);
}
