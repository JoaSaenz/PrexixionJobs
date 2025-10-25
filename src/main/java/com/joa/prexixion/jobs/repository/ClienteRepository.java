package com.joa.prexixion.jobs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    @Query(value = "SELECT c.ruc, c.solU, c.solC FROM cliente c WHERE c.idEstado = 1", nativeQuery = true)
    List<Cliente> obtenerClientesActivos();

    @Query(value = "SELECT c.ruc, c.solU, c.solC FROM cliente c LEFT JOIN signerNiveles n ON c.ruc = n.idCliente WHERE c.idEstado = :idEstado and c.y = :y and n.idNivelF = 4 and n.idNivelX3 = 5", nativeQuery = true)
    List<Cliente> obtenerClientesActivos(@Param("idEstado") Integer idEstado, @Param("y") Integer y);

    @Query(value = """
            SELECT c.ruc, c.solU, c.solC
            FROM cliente c
            WHERE c.ruc IN (
                '20602336141', '20602070922', '20602273432', '20612840602', '20612325503',
                '20539827864', '20606588284', '20609939134', '20611688734', '20614142465',
                '20613740865', '20609813645', '20607047295', '20604179115', '20603241755',
                '20602431755', '20600828925', '20553318395', '20481542805', '20481682135',
                '20481340765', '20477202145', '10746600785', '20440157816', '20608552236',
                '20611038306', '20612957267', '20604747377', '20482763007', '20445436217',
                '20481035458', '20482335978', '20482647198', '20602387888', '20609376938',
                '20609457148', '20610491678', '20610724508', '20613221108', '20614114399',
                '20611417749', '20610034099', '20608869329', '20608491539', '20604050139',
                '20605036709', '20601466229', '20600994159', '20481327319', '10179252649'
            )
            """, nativeQuery = true)
    List<Cliente> obtenerClientesTest10();

}
