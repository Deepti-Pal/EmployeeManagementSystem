package com.ems.model;

import java.time.LocalDateTime;

/**
 * Department - Represents a company department (maps to `departments` table).
 */
public class Department {

    private int    deptId;
    private String deptName;
    private String deptCode;
    private String location;
    private String managerName;
    private LocalDateTime createdAt;

    // Runtime-computed fields (from joins / aggregates)
    private int    totalEmployees;
    private int    activeEmployees;
    private double avgSalary;

    // ── Constructors ────────────────────────────────────────────────────────

    public Department() {}

    public Department(String deptName, String deptCode, String location, String managerName) {
        this.deptName    = deptName;
        this.deptCode    = deptCode;
        this.location    = location;
        this.managerName = managerName;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public int    getDeptId()     { return deptId; }
    public void   setDeptId(int v){ this.deptId = v; }

    public String getDeptName()   { return deptName; }
    public void   setDeptName(String v) { this.deptName = v; }

    public String getDeptCode()   { return deptCode; }
    public void   setDeptCode(String v) { this.deptCode = v; }

    public String getLocation()   { return location; }
    public void   setLocation(String v) { this.location = v; }

    public String getManagerName(){ return managerName; }
    public void   setManagerName(String v) { this.managerName = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public int    getTotalEmployees()  { return totalEmployees; }
    public void   setTotalEmployees(int v) { this.totalEmployees = v; }

    public int    getActiveEmployees() { return activeEmployees; }
    public void   setActiveEmployees(int v) { this.activeEmployees = v; }

    public double getAvgSalary()   { return avgSalary; }
    public void   setAvgSalary(double v) { this.avgSalary = v; }

    @Override
    public String toString() {
        return String.format(
            "Department{id=%d, code='%s', name='%s', location='%s', manager='%s', employees=%d}",
            deptId, deptCode, deptName, location, managerName, totalEmployees
        );
    }
}
