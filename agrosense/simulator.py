"""AgroSense sensor simulator. Usage: python simulator.py [userId] [password] [interval_seconds]
Registers the user if needed, logs in, then posts readings for 3 field devices."""
import sys, time, random, requests
BASE = "http://localhost:8000/api"
uid = sys.argv[1] if len(sys.argv) > 1 else "farmer1"
pw = sys.argv[2] if len(sys.argv) > 2 else "farmer123"
gap = float(sys.argv[3]) if len(sys.argv) > 3 else 3
s = requests.Session()
s.post(f"{BASE}/register", json={"userId": uid, "name": uid.title(), "password": pw})
r = s.post(f"{BASE}/login", json={"userId": uid, "password": pw})
r.raise_for_status()
state = {d: [random.uniform(24, 32), random.uniform(50, 70), random.uniform(35, 60)] for d in ("FIELD-A", "FIELD-B", "FIELD-C")}
print(f"Logged in as {uid}. Sending data every {gap}s. Ctrl+C to stop.")
while True:
    for dev, v in state.items():
        v[0] = min(45, max(15, v[0] + random.uniform(-0.6, 0.6)))
        v[1] = min(95, max(20, v[1] + random.uniform(-1.5, 1.5)))
        v[2] = max(5, v[2] - random.uniform(0, 1.2)) if random.random() > 0.05 else 70  # dries out, occasional irrigation
        p = {"deviceId": dev, "temperature": round(v[0], 1), "humidity": round(v[1], 1), "soilMoisture": round(v[2], 1)}
        print(dev, s.post(f"{BASE}/telemetry", json=p).status_code, p)
    time.sleep(gap)
