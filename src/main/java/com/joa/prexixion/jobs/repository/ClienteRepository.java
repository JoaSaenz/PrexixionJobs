package com.joa.prexixion.jobs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.joa.prexixion.jobs.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    @Query(value = "SELECT c.ruc, c.y, c.solU, c.solC FROM cliente c WHERE c.idEstado IN (1,3,5,6,7,8,9) ORDER BY c.y ASC", nativeQuery = true)
    List<Cliente> obtenerClientes();

    @Query(value = """
            SELECT c.ruc, c.solU, c.solC, c.y
            FROM cliente c
            WHERE c.ruc IN (
                '10188990920', '20560043270'
            )
            """, nativeQuery = true)
    List<Cliente> obtenerClientesTest();

}
