package com.ems.service;

import com.ems.auth.AuthService;
import com.ems.dao.EmployeeDAO;
import com.ems.model.Employee;

import java.time.LocalDate;
import java.util.List;

/**
 * EmployeeService - Business logic layer between UI and DAO.
 * Handles input validation, authorization, and data rules.
 */
public class EmployeeService {

    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final AuthService auth   = AuthService.getInstance();

    // ── ADD ──────────────────────────────────────────────────────────────────

    public boolean addEmployee(Employee emp) {
        auth.requireWriteAccess();
        validateEmployee(emp);

        // Auto-generate emp_code if not set
        if (emp.getEmpCode() == null || emp.getEmpCode().isBlank()) {
            emp.setEmpCode(generateEmpCode());
        }

        return empDAO.addEmployee(emp);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateEmployee(Employee emp) {
        auth.requireWriteAccess();
        if (emp.getEmpId() <= 0) throw new IllegalArgumentException("Invalid employee ID for update.");
        validateEmployee(emp);
        return empDAO.updateEmployee(emp);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteEmployee(int empId) {
        auth.requireWriteAccess();
        if (empId <= 0) throw new IllegalArgumentException("Invalid employee ID.");
        Employee existing = empDAO.getEmployeeById(empId);
        if (existing == null) throw new IllegalArgumentException("Employee not found: ID=" + empId);
        return empDAO.deleteEmployee(empId);
    }

    // ── READ / SEARCH ────────────────────────────────────────────────────────

    public List<Employee> getAllEmployees() {
        return empDAO.getAllEmployees();
    }

    public Employee getEmployeeById(int id) {
        return empDAO.getEmployeeById(id);
    }

    public Employee getEmployeeByCode(String code) {
        return empDAO.getEmployeeByCode(code);
    }

    public List<Employee> searchEmployees(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllEmployees();
        return empDAO.searchEmployees(keyword.trim());
    }

    public List<Employee> getEmployeesByDepartment(int deptId) {
        return empDAO.getEmployeesByDepartment(deptId);
    }

    public int getTotalCount()          { return empDAO.getTotalEmployeeCount(); }
    public int getActiveCount()         { return empDAO.getCountByStatus("ACTIVE"); }
    public int getInactiveCount()       { return empDAO.getCountByStatus("INACTIVE"); }
    public int getOnLeaveCount()        { return empDAO.getCountByStatus("ON_LEAVE"); }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validateEmployee(Employee emp) {
        if (emp.getFirstName() == null || emp.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (emp.getLastName() == null || emp.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required.");
        if (emp.getEmail() == null || !emp.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[a-z]{2,}$"))
            throw new IllegalArgumentException("Invalid email address: " + emp.getEmail());
        if (emp.getHireDate() == null)
            throw new IllegalArgumentException("Hire date is required.");
        if (emp.getHireDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Hire date cannot be in the future.");
        if (emp.getDeptId() <= 0)
            throw new IllegalArgumentException("A valid department must be selected.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateEmpCode() {
        int total = empDAO.getTotalEmployeeCount();
        return String.format("EMP%03d", total + 1);
    }
}
