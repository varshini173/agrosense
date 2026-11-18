package com.agrosense;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "telemetry")
public class Telemetry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String owner;     // users.user_id of the farmer
    @Column(nullable = false) public String deviceId;
    public double temperature, humidity, soilMoisture;
    public Instant recordedAt = Instant.now();
}
