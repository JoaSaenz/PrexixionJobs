package com.joa.prexixion.jobs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joa.prexixion.jobs.model.JobStatus;

public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {
    Optional<JobStatus> findTopByNombreJobOrderByHoraInicioDesc(String nombreJob);

    List<JobStatus> findTop7ByNombreJobOrderByHoraInicioDesc(String nombreJob);
}
