package com.ems.dao;

import com.ems.model.Department;
import com.ems.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartmentDAO - CRUD operations for the departments table.
 */
public class DepartmentDAO {

    private static final String INSERT_DEPT =
        "INSERT INTO departments (dept_name, dept_code, location, manager_name) VALUES (?,?,?,?)";
    private static final String UPDATE_DEPT =
        "UPDATE departments SET dept_name=?, dept_code=?, location=?, manager_name=? WHERE dept_id=?";
    private static final String DELETE_DEPT =
        "DELETE FROM departments WHERE dept_id=?";
    private static final String SELECT_ALL =
        "SELECT d.*, " +
        "  COUNT(e.emp_id) AS total_employees, " +
        "  SUM(CASE WHEN e.status='ACTIVE' THEN 1 ELSE 0 END) AS active_employees " +
        "FROM departments d LEFT JOIN employees e ON d.dept_id = e.dept_id " +
        "GROUP BY d.dept_id ORDER BY d.dept_name";
    private static final String SELECT_BY_ID =
        "SELECT * FROM departments WHERE dept_id=?";

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean addDepartment(Department dept) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_DEPT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getDeptCode());
            ps.setString(3, dept.getLocation());
            ps.setString(4, dept.getManagerName());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) dept.setDeptId(keys.getInt(1));
                System.out.println("[DAO] Department added: " + dept.getDeptName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] addDepartment error: " + e.getMessage());
        }
        return false;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Department d = mapRow(rs);
                d.setTotalEmployees(rs.getInt("total_employees"));
                d.setActiveEmployees(rs.getInt("active_employees"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] getAllDepartments error: " + e.getMessage());
        }
        return list;
    }

    public Department getDepartmentById(int deptId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, deptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] getDepartmentById error: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateDepartment(Department dept) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_DEPT)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getDeptCode());
            ps.setString(3, dept.getLocation());
            ps.setString(4, dept.getManagerName());
            ps.setInt(5,    dept.getDeptId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Department updated: ID=" + dept.getDeptId());
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] updateDepartment error: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteDepartment(int deptId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_DEPT)) {
            ps.setInt(1, deptId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Department deleted: ID=" + deptId);
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteDepartment error: " + e.getMessage());
        }
        return false;
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private Department mapRow(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDeptId(rs.getInt("dept_id"));
        d.setDeptName(rs.getString("dept_name"));
        d.setDeptCode(rs.getString("dept_code"));
        d.setLocation(rs.getString("location"));
        d.setManagerName(rs.getString("manager_name"));
        return d;
    }
}
