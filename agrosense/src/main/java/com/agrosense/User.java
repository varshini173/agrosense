package com.agrosense;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true, nullable = false) public String userId;
    @Column(nullable = false) public String name;
    @Column(nullable = false) public String passwordHash;   // BCrypt hash, never plain text
    public Instant createdAt = Instant.now();
}
