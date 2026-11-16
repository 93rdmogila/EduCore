# EduCore — School Management System

EduCore is a console-based school management application written in Java, developed as a team project for the Software Design Patterns course (group SE-2526, Astana IT University). It supports student, teacher, and administrator accounts; grade management; notifications when grades are added or updated; and calculation of final grades using interchangeable strategies.

Repository: https://github.com/93rdmogila/EduCore

## Main features

- Student self-registration and login.
- First administrator setup on the first launch; administrators can register teachers and other administrators.
- Role-specific console menus for students, teachers, and administrators.
- Teachers can add grades and update only grades they originally assigned.
- Students can view their grades, notifications, and final grades.
- Final grade calculation using simple average, weighted average, or highest grade.
- JSON-based file persistence for users, accounts, grades, and notifications.
- Password hashing using PBKDF2WithHmacSHA256.

## Technologies

- Java 17
- Maven
- JUnit 5
- JSON persistence implemented in the project (no external JSON library)

## Requirements

- JDK 17 or later
- Apache Maven

## Run the application

Open a terminal in the `educore` project directory and run:

```bash
mvn compile exec:java -Dexec.mainClass="com.educore.Main"
```

On the first launch, the application asks you to create the first administrator account. After that, use the displayed menu to log in.

## Run tests

```bash
mvn test
```

The project contains unit tests for academic operations, authentication/registration, and JSON storage.

## Roles and permissions

| Role | Main capabilities |
|---|---|
| Student | View own grades, notifications, and final grade |
| Teacher | View students, add grades, update own grades, view grades and calculate final grades |
| Administrator | Register teachers/administrators, list students and teachers, view grades and calculate final grades |

Students can register themselves. Teacher and administrator registration is restricted to an administrator, except for the first administrator created during initial setup.

## Design patterns

The project integrates six design patterns:

1. **Abstract Factory** — `StudentFactory`, `TeacherFactory`, and `AdministratorFactory` create role-specific user objects and corresponding `UserAccount` objects.
2. **Builder** — `StudentProfileBuilder` constructs a `StudentProfile` with required and optional fields and validates required data in `build()`.
3. **Observer** — `GradePublisher` notifies `GradeObserver` implementations; `StudentNotificationObserver` creates persistent notifications when a student's grade is added or updated.
4. **Strategy** — `GradeCalculationStrategy` defines the calculation contract; `SimpleAverageStrategy`, `WeightedAverageStrategy`, and `HighestGradeStrategy` implement alternative calculations.
5. **Decorator** — role-specific access decorators extend `BasicAccess` permissions through the `Access` abstraction.
6. **Facade** — `SchoolFacade` provides a simplified entry point to registration, authentication, grade management, permission checks, and final-grade calculation.

## Project structure

```text
src/main/java/com/educore/
├── Main.java
├── access/                 # Decorator and permission checks
├── academic/
│   ├── model/              # Grade, GradeType, Notification
│   ├── observer/           # Observer pattern
│   ├── repository/         # Grade and notification persistence
│   ├── service/            # GradeService
│   └── strategy/           # Grade calculation strategies
├── facade/                 # SchoolFacade
├── storage/                # JSON parser and file utilities
└── user/
    ├── auth/               # Registration, authentication, sessions, hashing
    ├── factory/            # Abstract Factory participants
    ├── model/              # User models and StudentProfileBuilder
    ├── repository/         # User persistence
    └── util/               # Validation
```

## Data storage

At runtime, JSON files are created in the `educore/data` directory relative to the working directory:

- `users.json`
- `accounts.json`
- `grades.json`
- `notifications.json`

The `educore/data` directory is created automatically on first launch. `.gitignore` excludes `educore/data` and `target/`, so runtime data and build output are not committed.

## Known limitations and future improvements

- Password entry is currently visible in the console.
- The system does not yet assign students to specific teacher-managed classes; consequently, the current teacher workflow does not enforce class membership when selecting a student.
- The first person who initializes an empty data store can create the first administrator account.
- Future work: class management (`SchoolClass`, teacher/student assignment, `classes.json`), hidden password input, login-attempt lockout, a maintained JSON library such as Jackson, and permission management from the menu.

## Team contribution

| Team member | Implemented patterns |
|---|---|
| Yerzhanov Amanzhol | Abstract Factory, Builder |
| Zhakanov Sanzhar | Observer, Strategy |
| Aytimov Yerzhan | Decorator, Facade |
