# Employee Management System
### Java | MySQL | JDBC

---

## Project Overview
A console-based **Employee Management System** built with **Java** and **MySQL** demonstrating enterprise-grade software patterns including:
- CRUD Operations via JDBC
- DAO (Data Access Object) design pattern
- Service Layer with business logic and validation
- Role-Based Access Control (RBAC)
- SHA-256 password hashing (no plain-text passwords)
- Relational database design with Foreign Keys, Views, Indexes

---

## Project Structure
```
EmployeeManagementSystem/
├── database/
│   └── schema.sql                    ← Run this first in MySQL
│
└── src/com/ems/
    ├── model/
    │   ├── Employee.java             ← Entity class
    │   ├── Department.java
    │   ├── SalaryRecord.java
    │   └── User.java
    │
    ├── dao/
    │   ├── EmployeeDAO.java          ← JDBC CRUD for employees
    │   ├── DepartmentDAO.java
    │   ├── SalaryDAO.java
    │   └── UserDAO.java
    │
    ├── service/
    │   └── EmployeeService.java      ← Business logic + validation
    │
    ├── auth/
    │   └── AuthService.java          ← Login session + RBAC
    │
    ├── reports/
    │   └── ReportService.java        ← Dashboard & analytics
    │
    ├── util/
    │   ├── DatabaseConnection.java   ← Singleton JDBC connector
    │   └── PasswordUtil.java         ← SHA-256 hashing
    │
    └── ui/
        └── MainApp.java              ← Entry point (console menu)
```

---

## Prerequisites
| Tool | Version |
|------|---------|
| Java | 11 or higher |
| MySQL | 8.0 or higher |
| MySQL JDBC Driver | mysql-connector-java-8.x.jar |

Download JDBC Driver: https://dev.mysql.com/downloads/connector/j/

---

## Setup Instructions

### Step 1 – MySQL Setup
```sql
-- Open MySQL Workbench or command line:
mysql -u root -p

-- Then run the schema file:
source /path/to/database/schema.sql
```

### Step 2 – Configure Database Connection
Edit `src/com/ems/util/DatabaseConnection.java`:
```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/employee_management_db";
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "your_mysql_password";   // ← change this
```

### Step 3 – Compile
```bash
# From the src/ directory:
javac -cp ".;path/to/mysql-connector-java.jar" com/ems/ui/MainApp.java

# On Linux/Mac use : instead of ;
javac -cp ".:path/to/mysql-connector-java.jar" com/ems/ui/MainApp.java
```

### Step 4 – Run
```bash
java -cp ".;path/to/mysql-connector-java.jar" com.ems.ui.MainApp

# Linux/Mac:
java -cp ".:path/to/mysql-connector-java.jar" com.ems.ui.MainApp
```

---

## Default Login Credentials
| Username | Password | Role |
|----------|----------|------|
| `admin`  | `admin123` | ADMIN |
| `hr_user`| `admin123` | HR |

---

## Features & Functionality

### 1. Login Authentication
- SHA-256 hashed passwords (no plain text storage)
- 3-attempt lockout
- Role-based access: ADMIN, HR, VIEWER
- Session tracking with last_login timestamp

### 2. Employee Management
| Operation | Description |
|-----------|-------------|
| Add | Add employee with validation (email format, dates) |
| View All | Tabular list with department info |
| Search | Search by name, email, code, or designation |
| Update | Edit any field, press Enter to keep current |
| Delete | Confirmation prompt before deletion |

### 3. Department Management
- Add / Update / Delete departments (ADMIN only)
- View with employee count statistics

### 4. Salary Records
- Add salary records with: Basic, HRA, DA, Allowances, Deductions
- Net salary auto-calculated: `Basic + HRA + DA + Allowances - Deductions`
- View all records or filter by employee

### 5. Reports Dashboard
- Dashboard Summary (totals, averages)
- Full Employee Report (formatted table)
- Department Report (with headcount)
- Salary Report (with grand total payroll)
- Top N Earners ranking

---

## Database Schema (ERD Summary)
```
departments ──< employees >── salary_records
                    │
                 users (linked optionally)
```

### Key Tables
| Table | Purpose |
|-------|---------|
| `departments` | Company departments |
| `employees` | Employee master records |
| `salary_records` | Monthly salary breakdown |
| `users` | Login credentials |

### SQL Views
- `vw_employee_details` – Employee + Dept + Salary joined
- `vw_department_summary` – Department headcount + avg salary

---

## OOP Concepts Used
| Concept | Where Used |
|---------|-----------|
| **Encapsulation** | All model classes (private fields + getters/setters) |
| **Abstraction** | DAO layer hides SQL from Service layer |
| **Singleton** | `DatabaseConnection`, `AuthService` |
| **Separation of Concerns** | Model → DAO → Service → UI layers |
| **Validation** | `EmployeeService.validateEmployee()` |
| **Error Handling** | Try-catch in all DAO methods |

---

## Resume Points (Proven by Code)
✅ **Java** – Full Java 11+ application  
✅ **OOP** – Models, encapsulation, singleton, layered architecture  
✅ **MySQL** – Schema design, FK constraints, Views, Indexes  
✅ **SQL Queries** – PreparedStatements, JOINs, GROUP BY, aggregations  
✅ **CRUD Operations** – All 4 operations across all entities  
✅ **Database Design** – Normalized 3NF schema  
✅ **Problem Solving** – Validation, auth, role control  
✅ **Business Applications** – HR workflow with payroll and reporting  

---

*Built for portfolio demonstration – Accenture-aligned enterprise Java stack*
