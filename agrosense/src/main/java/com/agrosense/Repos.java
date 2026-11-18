package com.agrosense;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;
interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
interface TelemetryRepo extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findByOwnerAndRecordedAtBetweenOrderByRecordedAtDesc(String owner, Instant from, Instant to, Pageable p);
}
