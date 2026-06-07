package com.ems.dao;

import com.ems.model.Employee;
import com.ems.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO - Data Access Object for all employee CRUD operations.
 * Demonstrates: PreparedStatements, ResultSet mapping, SQL queries.
 */
public class EmployeeDAO {

    // ── SQL Queries ──────────────────────────────────────────────────────────

    private static final String INSERT_EMPLOYEE =
        "INSERT INTO employees (emp_code, first_name, last_name, email, phone, " +
        "date_of_birth, hire_date, dept_id, designation, status) VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_EMPLOYEE =
        "UPDATE employees SET first_name=?, last_name=?, email=?, phone=?, " +
        "date_of_birth=?, hire_date=?, dept_id=?, designation=?, status=? WHERE emp_id=?";

    private static final String DELETE_EMPLOYEE =
        "DELETE FROM employees WHERE emp_id=?";

    private static final String SELECT_ALL =
        "SELECT e.*, d.dept_name FROM employees e " +
        "LEFT JOIN departments d ON e.dept_id = d.dept_id ORDER BY e.emp_id";

    private static final String SELECT_BY_ID =
        "SELECT e.*, d.dept_name FROM employees e " +
        "LEFT JOIN departments d ON e.dept_id = d.dept_id WHERE e.emp_id=?";

    private static final String SELECT_BY_CODE =
        "SELECT e.*, d.dept_name FROM employees e " +
        "LEFT JOIN departments d ON e.dept_id = d.dept_id WHERE e.emp_code=?";

    private static final String SEARCH_EMPLOYEES =
        "SELECT e.*, d.dept_name FROM employees e " +
        "LEFT JOIN departments d ON e.dept_id = d.dept_id " +
        "WHERE e.first_name LIKE ? OR e.last_name LIKE ? OR e.email LIKE ? " +
        "OR e.emp_code LIKE ? OR e.designation LIKE ?";

    private static final String SELECT_BY_DEPT =
        "SELECT e.*, d.dept_name FROM employees e " +
        "LEFT JOIN departments d ON e.dept_id = d.dept_id WHERE e.dept_id=?";

    private static final String COUNT_ALL =
        "SELECT COUNT(*) FROM employees";

    private static final String COUNT_BY_STATUS =
        "SELECT COUNT(*) FROM employees WHERE status=?";

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Inserts a new employee and sets the generated emp_id.
     * @return true if insert succeeded
     */
    public boolean addEmployee(Employee emp) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_EMPLOYEE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,  emp.getEmpCode());
            ps.setString(2,  emp.getFirstName());
            ps.setString(3,  emp.getLastName());
            ps.setString(4,  emp.getEmail());
            ps.setString(5,  emp.getPhone());
            ps.setDate(6,    emp.getDateOfBirth() != null ? Date.valueOf(emp.getDateOfBirth()) : null);
            ps.setDate(7,    Date.valueOf(emp.getHireDate()));
            ps.setInt(8,     emp.getDeptId());
            ps.setString(9,  emp.getDesignation());
            ps.setString(10, emp.getStatus() != null ? emp.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) emp.setEmpId(keys.getInt(1));
                System.out.println("[DAO] Employee added: " + emp.getFullName() + " (ID=" + emp.getEmpId() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] addEmployee error: " + e.getMessage());
        }
        return false;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /** Fetches all employees with their department name. */
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getAllEmployees error: " + e.getMessage());
        }
        return list;
    }

    /** Fetches a single employee by primary key. */
    public Employee getEmployeeById(int empId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] getEmployeeById error: " + e.getMessage());
        }
        return null;
    }

    /** Fetches a single employee by emp_code. */
    public Employee getEmployeeByCode(String empCode) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CODE)) {
            ps.setString(1, empCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] getEmployeeByCode error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Full-text search across name, email, code, and designation.
     * @param keyword the search term
     */
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> list = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_EMPLOYEES)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] searchEmployees error: " + e.getMessage());
        }
        return list;
    }

    /** Returns all employees belonging to a specific department. */
    public List<Employee> getEmployeesByDepartment(int deptId) {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DEPT)) {
            ps.setInt(1, deptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getEmployeesByDepartment error: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Updates all mutable fields of an existing employee.
     * @return true if exactly one row was updated
     */
    public boolean updateEmployee(Employee emp) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_EMPLOYEE)) {

            ps.setString(1, emp.getFirstName());
            ps.setString(2, emp.getLastName());
            ps.setString(3, emp.getEmail());
            ps.setString(4, emp.getPhone());
            ps.setDate(5,   emp.getDateOfBirth() != null ? Date.valueOf(emp.getDateOfBirth()) : null);
            ps.setDate(6,   Date.valueOf(emp.getHireDate()));
            ps.setInt(7,    emp.getDeptId());
            ps.setString(8, emp.getDesignation());
            ps.setString(9, emp.getStatus());
            ps.setInt(10,   emp.getEmpId());

            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Employee updated: ID=" + emp.getEmpId());
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] updateEmployee error: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * Permanently deletes an employee record (cascades to salary_records).
     * @return true if the record was found and deleted
     */
    public boolean deleteEmployee(int empId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_EMPLOYEE)) {
            ps.setInt(1, empId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Employee deleted: ID=" + empId);
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteEmployee error: " + e.getMessage());
        }
        return false;
    }

    // ── Counts ───────────────────────────────────────────────────────────────

    public int getTotalEmployeeCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] count error: " + e.getMessage());
        }
        return 0;
    }

    public int getCountByStatus(String status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_BY_STATUS)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] countByStatus error: " + e.getMessage());
        }
        return 0;
    }

    // ── ResultSet Mapper ─────────────────────────────────────────────────────

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmpId(rs.getInt("emp_id"));
        emp.setEmpCode(rs.getString("emp_code"));
        emp.setFirstName(rs.getString("first_name"));
        emp.setLastName(rs.getString("last_name"));
        emp.setEmail(rs.getString("email"));
        emp.setPhone(rs.getString("phone"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) emp.setDateOfBirth(dob.toLocalDate());

        emp.setHireDate(rs.getDate("hire_date").toLocalDate());
        emp.setDeptId(rs.getInt("dept_id"));
        emp.setDeptName(rs.getString("dept_name"));
        emp.setDesignation(rs.getString("designation"));
        emp.setStatus(rs.getString("status"));
        return emp;
    }
}
