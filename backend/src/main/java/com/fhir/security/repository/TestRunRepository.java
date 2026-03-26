package com.fhir.security.repository;

import com.fhir.security.entity.TestRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    List<TestRun> findByServerIdOrderByStartedAtDesc(Long serverId);

    @EntityGraph(attributePaths = {"testResults", "testResults.scenario"})
    Optional<TestRun> findFirstByServer_IdOrderByStartedAtDesc(Long serverId);
}
