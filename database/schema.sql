-- ============================================
-- Employee Management System - Database Schema
-- Author: EMS Project
-- Database: MySQL
-- ============================================

CREATE DATABASE IF NOT EXISTS employee_management_db;
USE employee_management_db;

-- ============================================
-- TABLE: departments
-- ============================================
CREATE TABLE IF NOT EXISTS departments (
    dept_id       INT AUTO_INCREMENT PRIMARY KEY,
    dept_name     VARCHAR(100) NOT NULL UNIQUE,
    dept_code     VARCHAR(10)  NOT NULL UNIQUE,
    location      VARCHAR(100),
    manager_name  VARCHAR(100),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: employees
-- ============================================
CREATE TABLE IF NOT EXISTS employees (
    emp_id        INT AUTO_INCREMENT PRIMARY KEY,
    emp_code      VARCHAR(20)  NOT NULL UNIQUE,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(15),
    date_of_birth DATE,
    hire_date     DATE         NOT NULL,
    dept_id       INT,
    designation   VARCHAR(100),
    status        ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE') DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE SET NULL
);

-- ============================================
-- TABLE: salary_records
-- ============================================
CREATE TABLE IF NOT EXISTS salary_records (
    salary_id     INT AUTO_INCREMENT PRIMARY KEY,
    emp_id        INT          NOT NULL,
    basic_salary  DECIMAL(12,2) NOT NULL,
    hra           DECIMAL(12,2) DEFAULT 0.00,
    da            DECIMAL(12,2) DEFAULT 0.00,
    allowances    DECIMAL(12,2) DEFAULT 0.00,
    deductions    DECIMAL(12,2) DEFAULT 0.00,
    net_salary    DECIMAL(12,2) GENERATED ALWAYS AS (basic_salary + hra + da + allowances - deductions) STORED,
    effective_from DATE         NOT NULL,
    pay_month     VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- ============================================
-- TABLE: users (Login Authentication)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'HR', 'VIEWER') DEFAULT 'VIEWER',
    emp_id        INT,
    is_active     BOOLEAN DEFAULT TRUE,
    last_login    TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE SET NULL
);

-- ============================================
-- INDEXES for performance
-- ============================================
CREATE INDEX idx_emp_dept    ON employees(dept_id);
CREATE INDEX idx_emp_status  ON employees(status);
CREATE INDEX idx_emp_email   ON employees(email);
CREATE INDEX idx_salary_emp  ON salary_records(emp_id);
CREATE INDEX idx_salary_date ON salary_records(effective_from);

-- ============================================
-- SEED DATA - Departments
-- ============================================
INSERT INTO departments (dept_name, dept_code, location, manager_name) VALUES
('Information Technology', 'IT',  'Building A', 'Rajesh Kumar'),
('Human Resources',        'HR',  'Building B', 'Priya Sharma'),
('Finance',                'FIN', 'Building C', 'Amit Verma'),
('Marketing',              'MKT', 'Building A', 'Sunita Singh'),
('Operations',             'OPS', 'Building D', 'Vikram Patel');

-- ============================================
-- SEED DATA - Admin User (password: admin123)
-- SHA-256 hash of "admin123"
-- ============================================
INSERT INTO users (username, password_hash, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('hr_user', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'HR');

-- ============================================
-- SAMPLE EMPLOYEES
-- ============================================
INSERT INTO employees (emp_code, first_name, last_name, email, phone, date_of_birth, hire_date, dept_id, designation, status) VALUES
('EMP001', 'Rahul',   'Gupta',   'rahul.gupta@company.com',   '9876543210', '1990-05-15', '2020-01-10', 1, 'Software Engineer',    'ACTIVE'),
('EMP002', 'Anita',   'Sharma',  'anita.sharma@company.com',  '9876543211', '1988-08-22', '2019-03-15', 1, 'Senior Developer',     'ACTIVE'),
('EMP003', 'Priya',   'Verma',   'priya.verma@company.com',   '9876543212', '1992-11-30', '2021-06-01', 2, 'HR Executive',         'ACTIVE'),
('EMP004', 'Rohit',   'Singh',   'rohit.singh@company.com',   '9876543213', '1985-03-10', '2018-09-20', 3, 'Finance Analyst',      'ACTIVE'),
('EMP005', 'Kavita',  'Patel',   'kavita.patel@company.com',  '9876543214', '1993-07-18', '2022-02-14', 4, 'Marketing Executive',  'ACTIVE');

-- ============================================
-- SAMPLE SALARY RECORDS
-- ============================================
INSERT INTO salary_records (emp_id, basic_salary, hra, da, allowances, deductions, effective_from, pay_month) VALUES
(1, 50000.00, 20000.00, 5000.00, 3000.00, 4000.00, '2024-01-01', 'January 2024'),
(2, 75000.00, 30000.00, 7500.00, 5000.00, 6000.00, '2024-01-01', 'January 2024'),
(3, 45000.00, 18000.00, 4500.00, 2500.00, 3500.00, '2024-01-01', 'January 2024'),
(4, 60000.00, 24000.00, 6000.00, 4000.00, 5000.00, '2024-01-01', 'January 2024'),
(5, 40000.00, 16000.00, 4000.00, 2000.00, 3000.00, '2024-01-01', 'January 2024');

-- ============================================
-- USEFUL VIEWS
-- ============================================
CREATE OR REPLACE VIEW vw_employee_details AS
SELECT
    e.emp_id,
    e.emp_code,
    CONCAT(e.first_name, ' ', e.last_name) AS full_name,
    e.email,
    e.phone,
    e.designation,
    e.hire_date,
    e.status,
    d.dept_name,
    d.dept_code,
    d.location,
    s.basic_salary,
    s.net_salary
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
LEFT JOIN salary_records s ON e.emp_id = s.emp_id
    AND s.salary_id = (SELECT MAX(s2.salary_id) FROM salary_records s2 WHERE s2.emp_id = e.emp_id);

CREATE OR REPLACE VIEW vw_department_summary AS
SELECT
    d.dept_id,
    d.dept_name,
    d.dept_code,
    d.location,
    d.manager_name,
    COUNT(e.emp_id) AS total_employees,
    SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_employees,
    AVG(s.net_salary) AS avg_salary
FROM departments d
LEFT JOIN employees e ON d.dept_id = e.dept_id
LEFT JOIN salary_records s ON e.emp_id = s.emp_id
GROUP BY d.dept_id, d.dept_name, d.dept_code, d.location, d.manager_name;

SELECT 'Database setup complete!' AS status;
