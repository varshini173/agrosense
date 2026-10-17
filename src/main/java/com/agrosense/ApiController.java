package com.agrosense;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController @RequestMapping("/api")
public class ApiController {
    record Reading(String deviceId, double temperature, double humidity, double soilMoisture) {}
    private final UserRepo users; private final TelemetryRepo tele;
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    public ApiController(UserRepo u, TelemetryRepo t) { users = u; tele = t; }

    private static ResponseEntity<?> err(int code, String msg) { return ResponseEntity.status(code).body(Map.of("error", msg)); }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> b) {
        String id = b.getOrDefault("userId", "").trim(), name = b.getOrDefault("name", "").trim(), pw = b.getOrDefault("password", "");
        if (id.isEmpty() || name.isEmpty() || pw.length() < 6) return err(400, "User ID, name and a password of 6+ characters are required");
        if (users.existsByUserId(id)) return err(409, "User ID already taken");
        User u = new User(); u.userId = id; u.name = name; u.passwordHash = enc.encode(pw);
        users.save(u);
        return ResponseEntity.ok(Map.of("message", "Registered"));
    }

    @PostMapping("/login")   // credentials are checked against the PostgreSQL users table only
    public ResponseEntity<?> login(@RequestBody Map<String, String> b, HttpSession s) {
        Optional<User> u = users.findByUserId(b.getOrDefault("userId", "").trim())
                .filter(x -> enc.matches(b.getOrDefault("password", ""), x.passwordHash));
        if (u.isEmpty()) return err(401, "Invalid user ID or password");
        s.setAttribute("uid", u.get().userId);
        return ResponseEntity.ok(Map.of("userId", u.get().userId, "name", u.get().name));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession s) { s.invalidate(); return Map.of("message", "Logged out"); }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession s) {
        String uid = (String) s.getAttribute("uid");
        if (uid == null) return err(401, "Not logged in");
        return users.findByUserId(uid).<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("userId", u.userId, "name", u.name)))
                .orElseGet(() -> err(401, "Not logged in"));
    }

    @PostMapping("/telemetry")
    public ResponseEntity<?> add(@RequestBody Reading r, HttpSession s) {
        String uid = (String) s.getAttribute("uid");
        if (uid == null) return err(401, "Not logged in");
        Telemetry t = new Telemetry(); t.owner = uid; t.deviceId = r.deviceId();
        t.temperature = r.temperature(); t.humidity = r.humidity(); t.soilMoisture = r.soilMoisture();
        tele.save(t);
        return ResponseEntity.ok(Map.of("id", t.id));
    }

    @GetMapping("/telemetry")
    public ResponseEntity<?> list(@RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                                  @RequestParam(defaultValue = "100") int limit, HttpSession s) {
        String uid = (String) s.getAttribute("uid");
        if (uid == null) return err(401, "Not logged in");
        Instant f = from != null ? from : Instant.EPOCH, t = to != null ? to : Instant.now().plus(1, ChronoUnit.DAYS);
        return ResponseEntity.ok(tele.findByOwnerAndRecordedAtBetweenOrderByRecordedAtDesc(uid, f, t, PageRequest.of(0, Math.min(limit, 1000))));
    }
}
