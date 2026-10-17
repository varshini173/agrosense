# AgroSense
Spring Boot + PostgreSQL + plain HTML/JS dashboard (Chart.js) + Python sensor simulator.

## Run
1. Install JDK 17+, Maven, PostgreSQL, Python 3 (`pip install requests`).
2. Create the DB (pgAdmin or psql):  `CREATE DATABASE agrosense;`
3. Set your Postgres username/password in `src/main/resources/application.properties`.
4. `mvn spring-boot:run`  (tables `users` and `telemetry` are created automatically)
5. Open http://localhost:8080 and register, or run `python simulator.py farmer1 farmer123`
   then log in with those credentials.

## Where the 36 marks live
- `users` table: id, user_id, name, password_hash (BCrypt), created_at. Login checks only this table.
- `telemetry` table: id, owner (user_id), device_id, temperature, humidity, soil_moisture, recorded_at.
- Each user sees only their own readings. Dashboard is inaccessible without login (API returns 401).
