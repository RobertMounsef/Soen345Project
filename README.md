# SOEN 345: Cloud-based Ticket Reservation Application

## 1. Requirements Analysis
### Functional Requirements (FR)
| ID | Requirement | User | Description | Justification |
| :--- | :--- | :--- | :--- | :--- |
| **FR1** | Registration | Customer | Users register via email or phone. | Essential for account security and ticket history. |
| **FR2** | Browse Events | Customer | View a list of all available events. | Core discovery feature for the user. |
| **FR3** | Search & Filter | Customer | Filter by date, location, or category. | Improves usability for large event databases. |
| **FR4** | Reservation | Customer | Users can reserve tickets digitally. | Primary objective of the application. |
| **FR5** | Cancellation | Customer | Users can cancel existing reservations. | Necessary for user flexibility and seat recovery. |
| **FR6** | Confirmations | Customer | Receive digital confirmation via email. | Provides proof of purchase for the user. |
| **FR7** | Event Management | Admin | Add, edit, or cancel events. | Allows organizers to maintain real-time data. |

### Non-Functional Requirements (NFR)
* **Performance:** The system should support concurrent users without performance degradation.
* **Availability:** The system must be cloud-based to ensure high availability.
* **Usability:** The UI should be simple and user-friendly.

---

## 2. System Architecture & Design
We utilize a **Decoupled Client-Server Architecture**:

* **Presentation Layer (Frontend):** Developed natively for **Android using Java and XML**. This ensures a highly responsive, Material-compliant, mobile-first professional UI.
* **Application Layer (Backend):** A **Spring Boot (Java 17)** RESTful API managing the business logic, reservation validation, and endpoint security. Optimized with **Asynchronous Processing** for non-blocking email notifications.
* **Data Layer:** Hosted on **Firebase Realtime Database** for NoSQL cloud-based persistent storage. Includes an automated **DatabaseSeeder** for rapid environment setup.

---

## 3. Software Development Method
We adopted the **Agile Scrum** methodology to manage this project.
* **Sprints:** Bi-weekly development cycles focused on iterative feature delivery between the frontend Android stream and Spring Boot backend stream.
* **Version Control:** Hosted on **GitHub** for collaborative coding, branching, and peer code reviews.

---

## 4. Software Testing & QA Strategy
Testing was strictly integrated into our SDLC to guarantee production-ready software delivery and achieve full code verification.

### Software Testing Methods & Results
We implemented rigorous testing suites (98 total tests) to satisfy our QA requirements:

| Test Category | Scope & Objectives | Tooling |
| :--- | :--- | :--- |
| **Unit & Component Tests** | Verified individual Java models, util functions, and isolated backend services (e.g. `EventService`, `UserService`) independent of external layers. All tests compile and pass with 100% success rate on local JVMs. | JUnit 5 & Mockito |
| **Integration Tests** | Validated interactions between subsystems, such as Android `SharedPreferences` persistence and cloud-based Firebase Realtime Database connections. Verified database seeders and repository linkages. | AndroidJUnit4 & Spring Boot Test |
| **Functional & Acceptance Tests** | Automated end-to-end user reservation flows, layout bounds checking, and navigational intents. Ensures the system meets all stakeholder acceptance criteria and functional UI requirements. | Espresso UI Testing Framework |

### Continuous Integration (CI/CD)
* **GitHub Actions** serves as our primary Automated CI tool. 
* Our customized pipeline (`ci.yml`) is triggered on every `push` and `pull_request` to the `main` branch.
* It provisions dual cloud-runners to simultaneously compile and execute both the **Java Spring Boot test suite** and the **Android local unit test suite**, effectively preventing regression bugs from being merged.

---

## 5. Project Tools
* **IDE:** Android Studio & IntelliJ IDEA
* **Language:** Java 17 (Spring Boot) & Android Java
* **Testing:** JUnit 5, Mockito, Espresso
* **CI/CD:** GitHub Actions
