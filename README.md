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
| **FR6** | Confirmations | Customer | Receive digital confirmation via email/SMS. | Provides proof of purchase for the user. |
| **FR7** | Event Management | Admin | Add, edit, or cancel events. | Allows organizers to maintain real-time data. |

### Non-Functional Requirements (NFR)
* **Performance:** The system should support concurrent users without performance degradation.
* **Availability:** The system must be cloud-based to ensure high availability.
* **Usability:** The UI should be simple and user-friendly.

---

## 2. System Architecture & Design
We utilize a **Decoupled 3-Layered Architecture**:

* **Presentation Layer:** Developed using **React.js** to ensure a type-safe, responsive, and professional UI.
* **Application Layer:** A **Java** backend managing the business logic, reservation validation, and RESTful API endpoints.
* **Data Layer:** A cloud-hosted database (e.g., MySQL via AWS RDS) for persistent storage of users and events.

### Design Elements


* **Architecture Diagram:** [Link will go here when completed]
* **ER Diagram:** (https://drive.google.com/file/d/1LYcdCFUCBTYCgRVb8Z5pxiwDWKllbz8b/view?usp=sharing)
* **UML Use Case Diagram:** [View Diagram](https://drive.google.com/file/d/1LJoI_UiU6BqSZz1dt07B8mZe3Ap7dmnr/view?usp=share_link)
* **UML Class Diagram:** [Link will go here when completed]
* **UML Sequence Diagram:** [Link will go here when completed]

---

## 3. Software Development Method
We have adopted the **Scrum** methodology to manage this project.
* **Sprints:** Two-week cycles focused on specific functional requirements.
* **Version Control:** Hosted on **GitHub** for collaborative coding and code reviews.

---

## 4. Software Testing & QA Strategy
Testing is integrated into our workflow to ensure high-quality software delivery.



| Test Level | Scope | Tooling |
| :--- | :--- | :--- |
| **Unit Testing** | Individual Java classes and methods. | JUnit 5 |
| **Integration Testing** | API-to-Database communication. | JUnit 5 & Mockito |
| **System Testing** | Full user reservation flow (Frontend to Backend). | Selenium / Manual Check |

### Continuous Integration (CI)


* **GitHub Actions** serves as our primary CI tool. 
* It automatically executes our **Unit** and **Integration** tests on every push to the repository to prevent regression.

---

## 5. Project Tools
* **IDE:** Android Studio / IntelliJ.
* **Language:** Java.
* **Testing:** JUnit 5.
* **CI/CD:** GitHub Actions.
