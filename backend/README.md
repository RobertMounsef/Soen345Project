# Backend - SOEN345 Project
This is the **backend** of the SOEN345 Cloud-based Ticket Reservation Application.  
It is built with **Spring Boot**, connects to **PostgreSQL**, and is designed to be deployed to the cloud.  
---
## Project Structure
```
backend/
├── .mvn/
│   └── wrapper/          # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/ticket/
│   │   │   ├── controller/    # REST controllers (API endpoints)
│   │   │   ├── model/         # JPA entity classes
│   │   │   ├── repository/    # Spring Data JPA repositories
│   │   │   ├── service/       # Service classes (business logic)
│   │   │   └── BackendApplication.java  # Spring Boot main class
│   │   └── resources/
│   │       └── application.properties  # Spring Boot config
│   └── test/
│       ├── java/com/ticket/
│       │   ├── unit/          # Unit tests
│       │   └── integration/   # Integration tests
│       └── resources/
│           └── application.properties  # H2 in-memory config for tests
├── .github/
│   └── workflows/
│       └── ci.yml            # GitHub Actions CI/CD
├── .gitignore
├── mvnw                      # Maven wrapper for Unix
├── mvnw.cmd                  # Maven wrapper for Windows
├── pom.xml                   # Maven build configuration
└── README.md
```
---
## Dependencies / Tools Needed
- **Java 17+** (ensure `JAVA_HOME` is set)  
- **Maven** (uses `mvnw` wrapper included in the project)  
- **Spring Boot 3**  
- **PostgreSQL** (local or cloud instance)  
---
## Project Set-up (Temporary for Local Hosting)
1. **Create the database**  
- Open PostgreSQL and create a database named `soen345db`.
2. **Update credentials**  
- In `src/main/resources/application.properties`, replace the placeholders with your actual credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/soen345db
spring.datasource.username=ACTUAL_USERNAME
spring.datasource.password=ACTUAL_PASSWORD
spring.mail.username=MAILTRAP_USERNAME
spring.mail.password=MAILTRAP_PASSWORD
spirng.mail.port= ACTUAL_PORT
```
3. **Navigate to backend folder**
- `cd SOEN345Project/backend`
4. **Run the application**
- `./mvnw spring-boot:run`
---
## Testing
Tests are written with **JUnit 5**, **Mockito**, and **Spring Boot Test**.

There are two separate `application.properties` files:
- `src/main/resources/application.properties` — real PostgreSQL + Mailtrap credentials, used when running the app
- `src/test/resources/application.properties` — H2 in-memory database + fake mail credentials, used automatically when running tests

To run all tests:
- `./mvnw test`

Tests also run automatically on every push and pull request to `main` via GitHub Actions.
