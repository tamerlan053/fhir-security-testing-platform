package com.fhir.security.repository;

import com.fhir.security.entity.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    List<TestRun> findByServerIdOrderByStartedAtDesc(Long serverId);
}
