SOEN345project/
└── backend/
    ├── .mvn/                   # Maven wrapper files (auto-generated)
    │   └── wrapper/
    │       └── maven-wrapper.properties
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/ticket/
    │   │   │       ├── controller/    # REST endpoints (APIs)
    │   │   │       ├── model/         # JPA entities (tables)
    │   │   │       ├── repository/    # Data access layer (Spring Data JPA)
    │   │   │       └── service/       # Business logic layer
    │   │   │       └── BackendApplication.java  # Main Spring Boot launcher
    │   │   └── resources/
    │   │       ├── application.properties       # Config for DB, Spring, logging
    │   │       ├── static/                      # For frontend static files (optional)
    │   │       └── templates/                   # For Thymeleaf templates (optional)
    │   └── test/                                 # Unit & integration tests
    ├── .gitignore                                # Files/folders Git should ignore
    ├── pom.xml                                   # Maven dependencies
    ├── mvnw / mvnw.cmd                            # Maven wrapper executables
    └── HELP.md / other docs                       # Optional project documentation