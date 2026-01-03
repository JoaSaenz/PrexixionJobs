package com.joa.prexixion.jobs.repository;

import com.joa.prexixion.jobs.model.JobStatus;
import com.joa.prexixion.jobs.model.JobStatusLog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusLogRepository extends JpaRepository<JobStatusLog, Long> {

    List<JobStatusLog> findByJobStatusOrderByFechaRegistroAsc(JobStatus jobStatus);
}
