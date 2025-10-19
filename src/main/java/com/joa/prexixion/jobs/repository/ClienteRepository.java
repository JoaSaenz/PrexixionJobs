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
    List<Cliente> obtenerClientesActivos(@Param("idEstado") Integer idEstado, @Param("y") Integer y);

    @Query(value = "SELECT c.ruc, c.solU, c.solC FROM cliente c WHERE c.ruc IN ('20602336141', '20602070922', '20602273432', '20612840602', '20612325503', '20539827864', '20606588284', '20609939134', '20611688734', '20614142465') ", nativeQuery = true)
    List<Cliente> obtenerClientesTest10();

}
