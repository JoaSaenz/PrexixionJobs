package com.joa.prexixion.jobs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joa.prexixion.jobs.model.JobStatus;

public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {
}
