package com.ems.dao;

import com.ems.model.SalaryRecord;
import com.ems.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SalaryDAO - CRUD operations for salary_records table.
 */
public class SalaryDAO {

    private static final String INSERT_SALARY =
        "INSERT INTO salary_records (emp_id, basic_salary, hra, da, allowances, deductions, effective_from, pay_month) " +
        "VALUES (?,?,?,?,?,?,?,?)";

    private static final String UPDATE_SALARY =
        "UPDATE salary_records SET basic_salary=?, hra=?, da=?, allowances=?, deductions=?, pay_month=? " +
        "WHERE salary_id=?";

    private static final String DELETE_SALARY =
        "DELETE FROM salary_records WHERE salary_id=?";

    private static final String SELECT_ALL =
        "SELECT s.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name, e.emp_code " +
        "FROM salary_records s JOIN employees e ON s.emp_id = e.emp_id ORDER BY s.salary_id DESC";

    private static final String SELECT_BY_EMP =
        "SELECT s.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name, e.emp_code " +
        "FROM salary_records s JOIN employees e ON s.emp_id = e.emp_id " +
        "WHERE s.emp_id=? ORDER BY s.effective_from DESC";

    private static final String SELECT_BY_ID =
        "SELECT s.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name, e.emp_code " +
        "FROM salary_records s JOIN employees e ON s.emp_id = e.emp_id WHERE s.salary_id=?";

    private static final String AVG_SALARY =
        "SELECT AVG(net_salary) FROM salary_records";

    private static final String TOTAL_PAYROLL =
        "SELECT SUM(net_salary) FROM salary_records WHERE pay_month=?";

    // ── CREATE ───────────────────────────────────────────────────────────────

    public boolean addSalaryRecord(SalaryRecord sr) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SALARY, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    sr.getEmpId());
            ps.setDouble(2, sr.getBasicSalary());
            ps.setDouble(3, sr.getHra());
            ps.setDouble(4, sr.getDa());
            ps.setDouble(5, sr.getAllowances());
            ps.setDouble(6, sr.getDeductions());
            ps.setDate(7,   Date.valueOf(sr.getEffectiveFrom()));
            ps.setString(8, sr.getPayMonth());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) sr.setSalaryId(keys.getInt(1));
                System.out.println("[DAO] Salary record added: ID=" + sr.getSalaryId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] addSalaryRecord error: " + e.getMessage());
        }
        return false;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<SalaryRecord> getAllSalaryRecords() {
        List<SalaryRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getAllSalaryRecords error: " + e.getMessage());
        }
        return list;
    }

    public List<SalaryRecord> getSalaryByEmployee(int empId) {
        List<SalaryRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMP)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getSalaryByEmployee error: " + e.getMessage());
        }
        return list;
    }

    public SalaryRecord getSalaryById(int salaryId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, salaryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] getSalaryById error: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateSalaryRecord(SalaryRecord sr) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SALARY)) {
            ps.setDouble(1, sr.getBasicSalary());
            ps.setDouble(2, sr.getHra());
            ps.setDouble(3, sr.getDa());
            ps.setDouble(4, sr.getAllowances());
            ps.setDouble(5, sr.getDeductions());
            ps.setString(6, sr.getPayMonth());
            ps.setInt(7,    sr.getSalaryId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Salary updated: ID=" + sr.getSalaryId());
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] updateSalaryRecord error: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteSalaryRecord(int salaryId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SALARY)) {
            ps.setInt(1, salaryId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) System.out.println("[DAO] Salary record deleted: ID=" + salaryId);
            return ok;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteSalaryRecord error: " + e.getMessage());
        }
        return false;
    }

    // ── Analytics ────────────────────────────────────────────────────────────

    public double getAverageSalary() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(AVG_SALARY);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getAverageSalary error: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalPayroll(String payMonth) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(TOTAL_PAYROLL)) {
            ps.setString(1, payMonth);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getTotalPayroll error: " + e.getMessage());
        }
        return 0;
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private SalaryRecord mapRow(ResultSet rs) throws SQLException {
        SalaryRecord sr = new SalaryRecord();
        sr.setSalaryId(rs.getInt("salary_id"));
        sr.setEmpId(rs.getInt("emp_id"));
        sr.setEmpName(rs.getString("emp_name"));
        sr.setEmpCode(rs.getString("emp_code"));
        sr.setBasicSalary(rs.getDouble("basic_salary"));
        sr.setHra(rs.getDouble("hra"));
        sr.setDa(rs.getDouble("da"));
        sr.setAllowances(rs.getDouble("allowances"));
        sr.setDeductions(rs.getDouble("deductions"));
        sr.setNetSalary(rs.getDouble("net_salary"));
        sr.setEffectiveFrom(rs.getDate("effective_from").toLocalDate());
        sr.setPayMonth(rs.getString("pay_month"));
        return sr;
    }
}
