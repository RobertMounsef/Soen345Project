# Backend — SOEN345 Ticket Reservation

Spring Boot REST API for the ticket reservation app. It uses **Firebase Realtime Database** for persistence and **SMTP** (Gmail, Mailtrap, etc.) for reservation emails.

---

## Project structure

```
backend/
├── src/main/java/com/ticket/
│   ├── controller/     # REST endpoints
│   ├── model/          # Domain types (User, Event, Reservation)
│   ├── repository/     # Firebase-backed repositories
│   ├── service/        # Business logic, email, optional SMS
│   └── config/
├── src/main/resources/
│   ├── application.properties                    # Safe defaults (safe to commit)
│   ├── application-local.properties.example      # Template for secrets (commit this)
│   ├── application-local.properties              # Your secrets (gitignored — create locally)
│   └── firebase-service-account.json             # Firebase Admin key (gitignored)
├── src/test/...
├── pom.xml
└── mvnw / mvnw.cmd
```

---

## Requirements

- **Java 17+** (`JAVA_HOME` set correctly; Android-style JDK issues on Windows: use a full JDK with `jlink`, e.g. Android Studio’s JBR)
- **Maven** (or use `./mvnw` / `mvnw.cmd` in this folder)
- **Firebase** project with Realtime Database enabled
- **SMTP** access if you want real emails (Gmail App Password or Mailtrap)

---

## Local setup

### 1. Firebase service account

1. In [Firebase Console](https://console.firebase.google.com) → Project settings → Service accounts → Generate new private key.
2. Save the JSON file as:

   `src/main/resources/firebase-service-account.json`

3. Do **not** commit this file. It is listed in `.gitignore` at the repo root and under `backend/.gitignore`.

### 2. Mail and other secrets (`application-local.properties`)

Shared settings (database URL, ports, etc.) live in **`application.properties`**. Anything secret or personal belongs in **`application-local.properties`**, which is **gitignored** and loaded automatically via:

`spring.config.import=optional:classpath:application-local.properties`

**Steps:**

1. Copy the example file:

   `src/main/resources/application-local.properties.example`
   → `src/main/resources/application-local.properties`

2. Edit `application-local.properties` and set at least:

   - **`spring.mail.username`** — e.g. your Gmail address
   - **`spring.mail.password`** — Gmail **App password** (Google Account → Security → App passwords), not your normal login password
   - **`app.mail.from`** — usually the same as `spring.mail.username` (or a verified “Send mail as” alias)

3. Optional: **Mailtrap** — use the SMTP user/password from your Mailtrap inbox instead; messages stay in Mailtrap’s UI only (good for testing).

4. Optional **Twilio** credentials can go in the same local file if you use SMS (`app.sms.*`).

If `application-local.properties` is missing, the app still starts, but mail credentials will be incomplete until you add that file.

### 3. Run

```bash
cd backend
./mvnw spring-boot:run        # Unix / Git Bash
mvnw.cmd spring-boot:run      # Windows cmd/PowerShell
```

API base URL is typically `http://localhost:8080` unless you change the port.

---

## What must not be pushed to GitHub

These paths are ignored; keep them **only** on your machine (or share secrets through a password manager / env vars, not the repo):

| Item | Purpose |
|------|--------|
| `firebase-service-account.json` | Firebase Admin SDK key |
| `application-local.properties` | Mail password, optional Twilio, overrides |
| `../android-app/**/local.properties` | Android SDK path (root `.gitignore`) |
| `**/google-services.json` | Firebase Android config (if present) |
| `*.jks`, `*.keystore`, `key.properties` | Signing keys |

**Committed** on purpose: `application.properties` (no secrets), `application-local.properties.example` (template only).

If a secret was ever committed, rotate it (new Gmail app password, new Firebase key) and use `git rm --cached <file>` so Git stops tracking the file while keeping it on disk.

---

## Testing

- **JUnit 5**, **Mockito**, **Spring Boot Test**
- **`src/test/resources/application.properties`** — stub SMTP settings for tests (no real mail required)

```bash
./mvnw test
```

Default test runs **exclude** tag **`firebase-integration`** (real Firebase Realtime Database). With **`firebase-service-account.json`** in `src/main/resources`, run repository integration tests against an isolated subtree:

```bash
./mvnw test -Pfirebase-it
```

CI runs `./mvnw test` only (see `.github/workflows/ci.yml` in the repo root), so Firebase ITs stay opt-in locally.

---

## API overview

See **`API_DOCUMENTATION.md`** in this folder for endpoints, request bodies, and session-based auth.
