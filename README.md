# heinze: The 100-Eyed Tactical Oracle 👁️🛡️

[![Architecture](https://img.shields.io/badge/Architecture-Clojure_Immutable-blueviolet?style=for-the-badge)](https://github.com/dennisgathu8/heinze)
[![Performance](https://img.shields.io/badge/Performance-Parallel_Simulation-orange?style=for-the-badge)](https://github.com/dennisgathu8/heinze)
[![Security](https://img.shields.io/badge/Security-Zero%20Dynamic%20Resolution-brightgreen?style=for-the-badge)](https://github.com/dennisgathu8/heinze)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

🟢 **Live Tactical Dashboard:** [https://heinze-oracle.fly.dev](https://heinze-oracle.fly.dev)

**heinze** (named after the legendary Gabriel Heinze) is a real-time tactical assistant that provides "ghost match" simulations for professional coaches. It leverages Clojure's immutable data structures to fork match realities in O(1) time.

### 🎯 The "Defensive Whisperer"
Current analytics are post-hoc. **heinze** is live.
- **O(1) Reality Forking:** Create a "ghost match" to test tactical changes ("What if we pressed high?") instantly without copying state.
- **Parallel Universes:** Run 1000+ Monte Carlo simulations lock-free via `pmap` for ultra-accurate probability forecasting.
- **Defensive Shape Correction:** Identifies vulnerabilities in the backline 3 seconds before the opponent exploits them.

### ⚽ Coaching Impact
- **Live Decision Support:** Real-time feedback for the technical bench via touchline tablets.
- **Immutable Audit Trail:** Every recommendation is logged with full tactical provenance.

---

## 🧑‍💻 About the Author

### Dennis Gathu

Systems-level thinker. I build things that are correct by construction, not by coincidence.

**Core Competencies:**

| Domain | Depth |
|--------|-------|
| **Systems Programming** | Kernel-level reasoning about memory layout, structural sharing, and persistent data structures. I think in pointers, not abstractions. |
| **Distributed Systems** | Consensus protocols, immutable state replication, lock-free concurrency via persistent vectors and `pmap`. No mutex, no deadlock, no problem. |
| **Security Engineering** | Zero-trust architecture from first principles. Static analysis at boot time. Distroless containers. If it has a shell, it has an attack surface. |
| **Functional Architecture** | Clojure, ClojureScript, Reagent/Re-frame. Pure functions as the unit of computation. Side effects quarantined to system boundaries. |
| **Low-Level Optimization** | Content-addressed deduplication (SHA-256), structural sharing verification (`identical?`), JVM memory profiling. 135k frames in <50MB. |
| **Build Systems & CI/CD** | Reproducible builds, deterministic dependency resolution (`deps.edn`), GitHub Actions pipelines with security-first audit gates. |
| **Version Control Internals** | Git as a content-addressed Merkle DAG — the same data structure that powers heinze's frame history. Not a coincidence. |
| **Cryptographic Integrity** | SHA-256 provenance chains, content hashing for deduplication, cryptographically seeded deterministic simulation. |

**Philosophy:**

> *"Talk is cheap. Show me the code."* — Linus Torvalds

I don't write abstractions for abstractions. Every line in this codebase exists because it solves a measurable problem:
- `next-frame` exists because deep-copying 50MB of match state is unacceptable.
- `pmap` exists because `Thread.start()` with shared mutable state is a race condition waiting to happen.
- `secure-read-string` exists because `eval` is a CVE waiting to be filed.
- The security audit exists because trust is verified, not assumed.

**Contact:** [GitHub](https://github.com/dennisgathu8) · Open for collaborations with KPL clubs & academies.

---

<details>
<summary>📐 Full Technical Architecture & Design</summary>

## 🎯 The Impossible Claim
Current football analytics treats matches as event streams (lossy, mutable, post-hoc). heinze treats a match as a **persistent data structure** — a content-addressed graph where every player position, tactical formation, and decision point is structurally shared across time.

What this enables:
- **O(1) Time Travel:** Scrub through 135,000 frames (90 minutes @ 25fps) instantly via vector indexing.
- **O(1) Reality Forking:** Create "ghost matches" by creating a new root pointer to existing immutable data.
- **Parallel Universes:** Run 1000 Monte Carlo simulations lock-free via `pmap`. No locks, no race conditions.
- **$0 Deployment:** Runs on Fly.io's free tier in a Distroless container with no shell and no root.

## 🏗 Architecture: The Full Stack

| Component | Tech Stack | Responsibility |
|-------|--------|----------------|
| **Frontend UI** | Reagent (React 18), ClojureScript | Arsenal-themed tactical HUD. Connects via WebSocket, renders live frames and ghost alerts. |
| **Agent Alpha** | `argus.ingest` | Data ingestion with EDN Fortress validation and content-hash deduplication. |
| **Agent Beta** | `argus.pitch` | Immutable pitch state with verified structural sharing. |
| **Agent Gamma** | `argus.heinze` | Pure function defensive pattern recognition. |
| **Agent Delta** | `argus.fork` | O(1) forking, deterministic simulation, parallel Monte Carlo via `pmap`. |
| **Agent Epsilon** | `argus.voice` | Real-time WebSocket EDN broadcasting to touchline tablets. |
| **Agent Zeta** | `argus.secure` | Source code security audit, zero dynamic resolution enforcement. |
| **Edge Proxy** | Fly.io Edge | TLS termination (`wss://`), auto-stop/start ($0 idle cost routing). |

## 🛡 Security Invariants (Non-Negotiable)
- **Zero Dynamic Resolution:** No `eval`, `read-string`, `resolve`, or `load-string` in backend source. Verified at JVM boot.
- **EDN Fortress:** All external data validated via whitelist-only parser. Unknown tags throw immediately.
- **Immutable Audit Trail:** Every tactical recommendation logged with SHA-256 frame-hash provenance.
- **Distroless Fortress:** Production JVM runs in a Distroless container (no `/bin/sh`, UID 65534).

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Clojure CLI 1.11+
- Node.js 20+ (for frontend development)

### Development Boot
```bash
git clone https://github.com/dennisgathu8/heinze.git
cd heinze

# 1. Build the UI
npm install
npx shadow-cljs release app

# 2. Boot the Edge Server
clojure -M -m argus.main
# Dashboard available at http://localhost:8080
```

### Production Deployment (Fly.io)
We use a 3-stage Docker build: Node (CLJS compilation) → Clojure (Uberjar) → Distroless (Runtime).
```bash
fly launch --name heinze-oracle --region jnb
fly deploy
```

### Run Tests
```bash
clojure -M:test
# 13 tests, 34 assertions, 0 failures
```

## 🎮 The "Heinze Lens" Demo
During a live match, Argus detects patterns that human eyes miss:

**Minute 23:** `{:type :high-line-exposure :confidence 0.82}`

Coach Action — Fork reality at frame 1847:
```clojure
(fork/fork-at frame {:press-height 60 :line-depth 40} 42)
;; Run 1000 parallel Monte Carlos via pmap
;; Result: Clean sheet probability 89% vs. 76% current setup
```

## 📊 Verification Results
1. **Structural Sharing:** `identical?` proves unchanged data shares memory references. 135k frames in <50MB.
2. **Parallel Simulation:** 100 rollouts × 50 ticks = 5000 frames in ~60ms.
3. **Determinism:** Same seed + same params = identical ghost match output.
4. **Security Audit:** Zero forbidden tokens in source. System refuses to boot if violated.
5. **CI/CD:** GitHub Actions pipeline green — security audit + full test suite on every push.

</details>

---

## 📜 License
MIT License — See [LICENSE](LICENSE).
