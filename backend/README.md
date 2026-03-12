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
│   │       ├── static/        # Static files (optional)
│   │       ├── templates/     # Templates (optional)
│   │       └── application.properties  # Spring Boot configuration
│   └── test/                   # Unit and integration tests
├── .gitignore                  # Git ignore rules
├── mvnw                        # Maven wrapper for Unix
├── mvnw.cmd                    # Maven wrapper for Windows
├── pom.xml                     # Maven build configuration
└── README.md                   # Project documentation
```

---

## Dependencies / Tools Needed

- **Java 17+** (ensure `JAVA_HOME` is set)  
- **Maven** (uses `mvnw` wrapper included in the project)  
- **Spring Boot 4.0.3**  
- **PostgreSQL 14+** (local or cloud instance)  
- **Postman** (for testing API endpoints)  

---

## Project Set-up (Temporary for Local Hosting)

1. **Create the database**  
   - Open PostgreSQL or pgAdmin and create a database named `soen345db`.

2. **Update credentials**  
   - In `src/main/resources/application.properties`, replace the placeholders with your actual PostgreSQL credentials:

```properties
spring.datasource.username=ACTUAL_USERNAME
spring.datasource.password=ACTUAL_PASSWORD
```

3. **Navigate to backend folder**
- `cd SOEN345Project/backend`

4. Run the application
- `./mvnw spring-boot:run`


