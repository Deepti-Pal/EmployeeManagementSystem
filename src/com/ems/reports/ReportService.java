package com.ems.reports;

import com.ems.dao.DepartmentDAO;
import com.ems.dao.EmployeeDAO;
import com.ems.dao.SalaryDAO;
import com.ems.model.Department;
import com.ems.model.Employee;
import com.ems.model.SalaryRecord;
import com.ems.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ReportService - Generates console-based reports and analytics for the dashboard.
 * Demonstrates: complex SQL joins, aggregations, formatted output.
 */
public class ReportService {

    private final EmployeeDAO   empDAO  = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private final SalaryDAO     salDAO  = new SalaryDAO();

    // ── Dashboard Summary ────────────────────────────────────────────────────

    public void printDashboard() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║           EMPLOYEE MANAGEMENT SYSTEM                 ║");
        System.out.println("║                 REPORTS DASHBOARD                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        int total    = empDAO.getTotalEmployeeCount();
        int active   = empDAO.getCountByStatus("ACTIVE");
        int inactive = empDAO.getCountByStatus("INACTIVE");
        int onLeave  = empDAO.getCountByStatus("ON_LEAVE");
        double avgSal = salDAO.getAverageSalary();

        System.out.printf("%n  %-25s %d%n", "Total Employees:",    total);
        System.out.printf("  %-25s %d%n",   "Active:",            active);
        System.out.printf("  %-25s %d%n",   "Inactive:",          inactive);
        System.out.printf("  %-25s %d%n",   "On Leave:",          onLeave);
        System.out.printf("  %-25s ₹%,.2f%n","Average Net Salary:", avgSal);
        System.out.println();
    }

    // ── Employee Report ──────────────────────────────────────────────────────

    public void printEmployeeReport() {
        List<Employee> list = empDAO.getAllEmployees();
        System.out.println("\n" + "=".repeat(90));
        System.out.println(" EMPLOYEE REPORT");
        System.out.println("=".repeat(90));
        System.out.printf("%-6s %-10s %-25s %-30s %-20s %-10s%n",
            "ID", "Code", "Name", "Email", "Department", "Status");
        System.out.println("-".repeat(90));
        for (Employee e : list) {
            System.out.printf("%-6d %-10s %-25s %-30s %-20s %-10s%n",
                e.getEmpId(), e.getEmpCode(), e.getFullName(),
                e.getEmail(), e.getDeptName(), e.getStatus());
        }
        System.out.println("=".repeat(90));
        System.out.println(" Total: " + list.size() + " employees");
        System.out.println("=".repeat(90));
    }

    // ── Department Report ────────────────────────────────────────────────────

    public void printDepartmentReport() {
        List<Department> list = deptDAO.getAllDepartments();
        System.out.println("\n" + "=".repeat(75));
        System.out.println(" DEPARTMENT REPORT");
        System.out.println("=".repeat(75));
        System.out.printf("%-6s %-8s %-25s %-15s %-10s %-8s%n",
            "ID", "Code", "Name", "Location", "Total Emp", "Active");
        System.out.println("-".repeat(75));
        for (Department d : list) {
            System.out.printf("%-6d %-8s %-25s %-15s %-10d %-8d%n",
                d.getDeptId(), d.getDeptCode(), d.getDeptName(),
                d.getLocation(), d.getTotalEmployees(), d.getActiveEmployees());
        }
        System.out.println("=".repeat(75));
    }

    // ── Salary Report ────────────────────────────────────────────────────────

    public void printSalaryReport() {
        List<SalaryRecord> list = salDAO.getAllSalaryRecords();
        System.out.println("\n" + "=".repeat(95));
        System.out.println(" SALARY REPORT");
        System.out.println("=".repeat(95));
        System.out.printf("%-6s %-10s %-20s %-12s %-10s %-10s %-10s %-12s%n",
            "ID", "EmpCode", "Employee", "Basic", "HRA", "DA", "Deduction", "Net Salary");
        System.out.println("-".repeat(95));
        double grandTotal = 0;
        for (SalaryRecord sr : list) {
            System.out.printf("%-6d %-10s %-20s %-12.2f %-10.2f %-10.2f %-10.2f %-12.2f%n",
                sr.getSalaryId(), sr.getEmpCode(), sr.getEmpName(),
                sr.getBasicSalary(), sr.getHra(), sr.getDa(),
                sr.getDeductions(), sr.getNetSalary());
            grandTotal += sr.getNetSalary();
        }
        System.out.println("=".repeat(95));
        System.out.printf(" %-70s ₹%,.2f%n", "TOTAL PAYROLL:", grandTotal);
        System.out.println("=".repeat(95));
    }

    // ── Top Earners ──────────────────────────────────────────────────────────

    public void printTopEarners(int limit) {
        String sql = "SELECT e.emp_code, CONCAT(e.first_name,' ',e.last_name) AS name, " +
                     "d.dept_name, s.net_salary " +
                     "FROM salary_records s " +
                     "JOIN employees e ON s.emp_id = e.emp_id " +
                     "LEFT JOIN departments d ON e.dept_id = d.dept_id " +
                     "ORDER BY s.net_salary DESC LIMIT ?";
        System.out.println("\n TOP " + limit + " EARNERS");
        System.out.println("-".repeat(60));
        System.out.printf("%-5s %-10s %-22s %-18s %s%n", "Rank", "Code", "Name", "Dept", "Net Salary");
        System.out.println("-".repeat(60));
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                System.out.printf("%-5d %-10s %-22s %-18s ₹%,.2f%n",
                    rank++, rs.getString("emp_code"),
                    rs.getString("name"), rs.getString("dept_name"),
                    rs.getDouble("net_salary"));
            }
        } catch (SQLException e) {
            System.err.println("[Report] topEarners error: " + e.getMessage());
        }
        System.out.println("-".repeat(60));
    }
}
