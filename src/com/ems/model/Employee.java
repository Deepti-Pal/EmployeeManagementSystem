package com.ems.model;

import java.time.LocalDate;

/**
 * Employee - Represents an employee entity (maps to `employees` table).
 * Demonstrates OOP: Encapsulation, getter/setter pattern, toString.
 */
public class Employee {

    private int    empId;
    private String empCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private int    deptId;
    private String deptName;      // joined from departments
    private String designation;
    private String status;        // ACTIVE | INACTIVE | ON_LEAVE

    // ── Constructors ────────────────────────────────────────────────────────

    public Employee() {}

    public Employee(String empCode, String firstName, String lastName,
                    String email, String phone, LocalDate dateOfBirth,
                    LocalDate hireDate, int deptId, String designation) {
        this.empCode     = empCode;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.email       = email;
        this.phone       = phone;
        this.dateOfBirth = dateOfBirth;
        this.hireDate    = hireDate;
        this.deptId      = deptId;
        this.designation = designation;
        this.status      = "ACTIVE";
    }

    // ── Derived helpers ─────────────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public int       getEmpId()        { return empId; }
    public void      setEmpId(int v)   { this.empId = v; }

    public String    getEmpCode()      { return empCode; }
    public void      setEmpCode(String v) { this.empCode = v; }

    public String    getFirstName()    { return firstName; }
    public void      setFirstName(String v) { this.firstName = v; }

    public String    getLastName()     { return lastName; }
    public void      setLastName(String v) { this.lastName = v; }

    public String    getEmail()        { return email; }
    public void      setEmail(String v){ this.email = v; }

    public String    getPhone()        { return phone; }
    public void      setPhone(String v){ this.phone = v; }

    public LocalDate getDateOfBirth()  { return dateOfBirth; }
    public void      setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }

    public LocalDate getHireDate()     { return hireDate; }
    public void      setHireDate(LocalDate v) { this.hireDate = v; }

    public int       getDeptId()       { return deptId; }
    public void      setDeptId(int v)  { this.deptId = v; }

    public String    getDeptName()     { return deptName; }
    public void      setDeptName(String v) { this.deptName = v; }

    public String    getDesignation()  { return designation; }
    public void      setDesignation(String v) { this.designation = v; }

    public String    getStatus()       { return status; }
    public void      setStatus(String v) { this.status = v; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Employee{id=%d, code='%s', name='%s', email='%s', dept='%s', designation='%s', status='%s'}",
            empId, empCode, getFullName(), email, deptName, designation, status
        );
    }
}
