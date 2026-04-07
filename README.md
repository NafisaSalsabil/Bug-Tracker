# Professional Bug Tracker

This repository contains a JavaFX-based desktop application designed for software defect management. The system integrates a stylized graphical user interface with a robust Oracle SQL backend to facilitate real-time bug reporting, status tracking, and data persistence.

---

## Features

* **Full CRUD Lifecycle**: Create, view, update, and delete bug records directly through the interface.
* **Advanced Filtering**: Real-time filtering logic using `FilteredList`, allowing users to sort by Status, Priority, Artifact Type, and Reporter simultaneously.
* **Global Search**: Instant keyword search functionality targeting both bug titles and descriptions.
* **Modern Dark UI**: A customized interface featuring a sidebar navigation, themed TableViews, and responsive layouts.
* **Automated Auditing**: The system automatically logs the `date_found` upon creation and timestamps the `date_fixed` column when a bug is marked as "Resolved."
* **Relational Integrity**: Built-in error handling for Oracle-specific constraints, such as unique titles and foreign key validation for reporters.

---

## Technical Stack

* **Language**: Java 21
* **Framework**: JavaFX (Controls, FXML)
* **Database**: Oracle Database (XE/PDB1)
* **Driver**: JDBC (OJDBC8)

---

## Directory Structure

* **Main.java**: The entry point of the application; manages the UI layout, CSS styling, and event handling.
* **CRUD.java**: The Data Access Object (DAO) layer containing all SQL logic, connection pooling, and database interactions.
* **Bug.java**: The model class defining the bug entity and its attributes for TableView binding.
* **[SQL Script]**: Contains the table definitions for `bugs` and `reporters`.

---

## Development Workflow

### Compilation
To compile the project and output the bytecode to the `out` directory, use the following command:

```bash
javac -cp ".;ojdbc8.jar" --module-path "C:\libetc\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml -d out *.java
```

### Execution
To launch the application from the compiled classes:

```bash
java -cp "out;ojdbc8.jar" --module-path "C:\libetc\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml Main
```

---

## Database Configuration

Before running the application, ensure your Oracle database is accessible at `localhost:1521/XEPDB1`. The application expects the following schema structure:

1. **Reporters Table**: Stores user identification and roles.
2. **Bugs Table**: Stores defect details with a foreign key relationship to the Reporters table.

If you need to update the database credentials, modify the `getConnection()` method within `CRUD.java`:

```java
String user = "System"; 
String password = "YourPassword";
```

---

## Usage Note

* **Add Bug**: Requires a valid `reporter_id` from the existing `reporters` table.
* **Update Status**: When changing a bug status to "Resolved," the system automatically populates the current date into the database.
* **Table Interactions**: Double-click any row in the table to view a detailed summary of the bug report.