package com.ems.ui;

import com.ems.auth.AuthService;
import com.ems.dao.DepartmentDAO;
import com.ems.dao.SalaryDAO;
import com.ems.model.*;
import com.ems.reports.ReportService;
import com.ems.service.EmployeeService;
import com.ems.util.DatabaseConnection;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * MainApp - Console UI entry point for the Employee Management System.
 *
 * Run with:
 *   javac -cp .;mysql-connector-java.jar com/ems/ui/MainApp.java
 *   java  -cp .;mysql-connector-java.jar com.ems.ui.MainApp
 *
 * Default credentials: admin / admin123
 */
public class MainApp {

    private static final Scanner sc             = new Scanner(System.in);
    private static final AuthService auth       = AuthService.getInstance();
    private static final EmployeeService empSvc = new EmployeeService();
    private static final DepartmentDAO  deptDAO = new DepartmentDAO();
    private static final SalaryDAO      salDAO  = new SalaryDAO();
    private static final ReportService  reports = new ReportService();

    public static void main(String[] args) {
        printBanner();

        // ── Step 1: Login ────────────────────────────────────────────────────
        if (!loginLoop()) {
            System.out.println("Too many failed attempts. Exiting.");
            return;
        }

        // ── Step 2: Main Menu Loop ───────────────────────────────────────────
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1  -> employeeMenu();
                case 2  -> departmentMenu();
                case 3  -> salaryMenu();
                case 4  -> reportsMenu();
                case 5  -> { auth.logout(); System.out.println("Logged out."); running = false; }
                default -> System.out.println("Invalid choice.");
            }
        }

        DatabaseConnection.closeConnection();
        System.out.println("Goodbye!");
    }

    // ── Login ────────────────────────────────────────────────────────────────

    private static boolean loginLoop() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("\n─── LOGIN (" + attempt + "/3) ─────────────────────────");
            String user = readString("Username: ");
            String pass = readString("Password: ");
            if (auth.login(user, pass)) {
                System.out.println("Welcome, " + auth.getCurrentUser().getUsername()
                    + " [" + auth.getCurrentRole() + "]");
                return true;
            }
            System.out.println("Invalid credentials.");
        }
        return false;
    }

    // ── Menus ────────────────────────────────────────────────────────────────

    private static void printMainMenu() {
        System.out.println("\n╔═══════════════════════════════╗");
        System.out.println("║      EMS - MAIN MENU          ║");
        System.out.println("╠═══════════════════════════════╣");
        System.out.println("║  1. Employee Management       ║");
        System.out.println("║  2. Department Management     ║");
        System.out.println("║  3. Salary Records            ║");
        System.out.println("║  4. Reports Dashboard         ║");
        System.out.println("║  5. Logout                    ║");
        System.out.println("╚═══════════════════════════════╝");
    }

    // ── EMPLOYEE MENU ────────────────────────────────────────────────────────

    private static void employeeMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Employee Management ──────────────────────");
            System.out.println("  1. View All Employees");
            System.out.println("  2. Search Employee");
            System.out.println("  3. Add Employee");
            System.out.println("  4. Update Employee");
            System.out.println("  5. Delete Employee");
            System.out.println("  6. Back");

            switch (readInt("Choice: ")) {
                case 1 -> listAllEmployees();
                case 2 -> searchEmployee();
                case 3 -> addEmployee();
                case 4 -> updateEmployee();
                case 5 -> deleteEmployee();
                case 6 -> back = true;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private static void listAllEmployees() {
        List<Employee> list = empSvc.getAllEmployees();
        System.out.println("\n" + "─".repeat(80));
        System.out.printf("%-5s %-10s %-22s %-25s %-18s %-8s%n",
            "ID", "Code", "Name", "Email", "Department", "Status");
        System.out.println("─".repeat(80));
        for (Employee e : list) {
            System.out.printf("%-5d %-10s %-22s %-25s %-18s %-8s%n",
                e.getEmpId(), e.getEmpCode(), e.getFullName(),
                e.getEmail(), e.getDeptName(), e.getStatus());
        }
        System.out.println("─".repeat(80));
        System.out.println(" Total: " + list.size());
    }

    private static void searchEmployee() {
        String kw = readString("Search (name / email / code): ");
        List<Employee> results = empSvc.searchEmployees(kw);
        if (results.isEmpty()) { System.out.println("No results found."); return; }
        System.out.println("\nFound " + results.size() + " result(s):");
        for (Employee e : results) {
            System.out.println("  " + e.getEmpCode() + " | " + e.getFullName()
                + " | " + e.getDesignation() + " | " + e.getDeptName() + " | " + e.getStatus());
        }
    }

    private static void addEmployee() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        System.out.println("\n── Add Employee ──");
        Employee emp = new Employee();
        emp.setFirstName(readString("First Name: "));
        emp.setLastName(readString("Last Name: "));
        emp.setEmail(readString("Email: "));
        emp.setPhone(readString("Phone: "));
        emp.setDesignation(readString("Designation: "));
        emp.setHireDate(readDate("Hire Date (YYYY-MM-DD): "));
        emp.setDateOfBirth(readDate("Date of Birth (YYYY-MM-DD, or blank): "));
        listDepartments();
        emp.setDeptId(readInt("Department ID: "));

        try {
            boolean ok = empSvc.addEmployee(emp);
            System.out.println(ok ? "✔ Employee added successfully! Code: " + emp.getEmpCode()
                                  : "✘ Failed to add employee.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateEmployee() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        int id = readInt("Employee ID to update: ");
        Employee emp = empSvc.getEmployeeById(id);
        if (emp == null) { System.out.println("Employee not found."); return; }

        System.out.println("Updating: " + emp.getFullName() + " (press Enter to keep current)");
        String fn = readStringOptional("First Name [" + emp.getFirstName() + "]: ");
        if (!fn.isBlank()) emp.setFirstName(fn);
        String ln = readStringOptional("Last Name [" + emp.getLastName() + "]: ");
        if (!ln.isBlank()) emp.setLastName(ln);
        String email = readStringOptional("Email [" + emp.getEmail() + "]: ");
        if (!email.isBlank()) emp.setEmail(email);
        String phone = readStringOptional("Phone [" + emp.getPhone() + "]: ");
        if (!phone.isBlank()) emp.setPhone(phone);
        String desig = readStringOptional("Designation [" + emp.getDesignation() + "]: ");
        if (!desig.isBlank()) emp.setDesignation(desig);
        System.out.println("Status options: ACTIVE, INACTIVE, ON_LEAVE");
        String status = readStringOptional("Status [" + emp.getStatus() + "]: ");
        if (!status.isBlank()) emp.setStatus(status.toUpperCase());

        try {
            boolean ok = empSvc.updateEmployee(emp);
            System.out.println(ok ? "✔ Employee updated." : "✘ Update failed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteEmployee() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        int id = readInt("Employee ID to delete: ");
        Employee emp = empSvc.getEmployeeById(id);
        if (emp == null) { System.out.println("Employee not found."); return; }

        System.out.print("Confirm delete '" + emp.getFullName() + "'? (yes/no): ");
        if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            boolean ok = empSvc.deleteEmployee(id);
            System.out.println(ok ? "✔ Employee deleted." : "✘ Delete failed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── DEPARTMENT MENU ──────────────────────────────────────────────────────

    private static void departmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Department Management ────────────────────");
            System.out.println("  1. View All Departments");
            System.out.println("  2. Add Department");
            System.out.println("  3. Update Department");
            System.out.println("  4. Delete Department");
            System.out.println("  5. Back");

            switch (readInt("Choice: ")) {
                case 1 -> listDepartmentsFull();
                case 2 -> addDepartment();
                case 3 -> updateDepartment();
                case 4 -> deleteDepartment();
                case 5 -> back = true;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private static void listDepartments() {
        System.out.println("\n Available Departments:");
        deptDAO.getAllDepartments().forEach(d ->
            System.out.printf("  [%d] %s - %s (%s)%n",
                d.getDeptId(), d.getDeptCode(), d.getDeptName(), d.getLocation()));
    }

    private static void listDepartmentsFull() {
        List<Department> list = deptDAO.getAllDepartments();
        System.out.println("\n" + "─".repeat(70));
        System.out.printf("%-5s %-8s %-22s %-15s %-12s %-6s%n",
            "ID", "Code", "Name", "Location", "Manager", "Emps");
        System.out.println("─".repeat(70));
        for (Department d : list) {
            System.out.printf("%-5d %-8s %-22s %-15s %-12s %-6d%n",
                d.getDeptId(), d.getDeptCode(), d.getDeptName(),
                d.getLocation(), d.getManagerName(), d.getTotalEmployees());
        }
        System.out.println("─".repeat(70));
    }

    private static void addDepartment() {
        if (!auth.isAdmin()) { System.out.println("Admin access required."); return; }
        System.out.println("\n── Add Department ──");
        Department dept = new Department(
            readString("Department Name: "),
            readString("Code (e.g. IT): "),
            readString("Location: "),
            readString("Manager Name: ")
        );
        System.out.println(deptDAO.addDepartment(dept) ? "✔ Department added." : "✘ Failed.");
    }

    private static void updateDepartment() {
        if (!auth.isAdmin()) { System.out.println("Admin access required."); return; }
        int id = readInt("Department ID to update: ");
        Department dept = deptDAO.getDepartmentById(id);
        if (dept == null) { System.out.println("Not found."); return; }

        String name = readStringOptional("Name [" + dept.getDeptName() + "]: ");
        if (!name.isBlank()) dept.setDeptName(name);
        String loc = readStringOptional("Location [" + dept.getLocation() + "]: ");
        if (!loc.isBlank()) dept.setLocation(loc);
        String mgr = readStringOptional("Manager [" + dept.getManagerName() + "]: ");
        if (!mgr.isBlank()) dept.setManagerName(mgr);

        System.out.println(deptDAO.updateDepartment(dept) ? "✔ Updated." : "✘ Failed.");
    }

    private static void deleteDepartment() {
        if (!auth.isAdmin()) { System.out.println("Admin access required."); return; }
        int id = readInt("Department ID to delete: ");
        System.out.print("Confirm? (yes/no): ");
        if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) { System.out.println("Cancelled."); return; }
        System.out.println(deptDAO.deleteDepartment(id) ? "✔ Deleted." : "✘ Failed.");
    }

    // ── SALARY MENU ──────────────────────────────────────────────────────────

    private static void salaryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Salary Records ───────────────────────────");
            System.out.println("  1. View All Salary Records");
            System.out.println("  2. View Salary by Employee ID");
            System.out.println("  3. Add Salary Record");
            System.out.println("  4. Update Salary Record");
            System.out.println("  5. Delete Salary Record");
            System.out.println("  6. Back");

            switch (readInt("Choice: ")) {
                case 1 -> printSalaries(salDAO.getAllSalaryRecords());
                case 2 -> { int eid = readInt("Employee ID: "); printSalaries(salDAO.getSalaryByEmployee(eid)); }
                case 3 -> addSalary();
                case 4 -> updateSalary();
                case 5 -> deleteSalary();
                case 6 -> back = true;
                default -> System.out.println("Invalid.");
            }
        }
    }

    private static void printSalaries(List<SalaryRecord> list) {
        if (list.isEmpty()) { System.out.println("No records found."); return; }
        System.out.println("\n" + "─".repeat(85));
        System.out.printf("%-5s %-10s %-20s %-12s %-10s %-10s %-12s%n",
            "ID", "EmpCode", "Employee", "Basic", "HRA+DA", "Deduct", "Net Salary");
        System.out.println("─".repeat(85));
        for (SalaryRecord sr : list) {
            System.out.printf("%-5d %-10s %-20s %-12.2f %-10.2f %-10.2f %-12.2f%n",
                sr.getSalaryId(), sr.getEmpCode(), sr.getEmpName(),
                sr.getBasicSalary(), sr.getHra() + sr.getDa(),
                sr.getDeductions(), sr.getNetSalary());
        }
        System.out.println("─".repeat(85));
    }

    private static void addSalary() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        System.out.println("\n── Add Salary Record ──");
        int empId      = readInt("Employee ID: ");
        double basic   = readDouble("Basic Salary: ");
        double hra     = readDouble("HRA: ");
        double da      = readDouble("DA: ");
        double allow   = readDouble("Allowances: ");
        double deduct  = readDouble("Deductions: ");
        LocalDate from = readDate("Effective From (YYYY-MM-DD): ");
        String month   = readString("Pay Month (e.g. June 2024): ");

        SalaryRecord sr = new SalaryRecord(empId, basic, hra, da, allow, deduct, from, month);
        System.out.println(salDAO.addSalaryRecord(sr)
            ? "✔ Salary record added. Net: ₹" + String.format("%,.2f", sr.getNetSalary())
            : "✘ Failed.");
    }

    private static void updateSalary() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        int id = readInt("Salary Record ID to update: ");
        SalaryRecord sr = salDAO.getSalaryById(id);
        if (sr == null) { System.out.println("Not found."); return; }

        sr.setBasicSalary(readDoubleOptional("Basic [" + sr.getBasicSalary() + "]: ", sr.getBasicSalary()));
        sr.setHra(readDoubleOptional("HRA [" + sr.getHra() + "]: ", sr.getHra()));
        sr.setDa(readDoubleOptional("DA [" + sr.getDa() + "]: ", sr.getDa()));
        sr.setAllowances(readDoubleOptional("Allowances [" + sr.getAllowances() + "]: ", sr.getAllowances()));
        sr.setDeductions(readDoubleOptional("Deductions [" + sr.getDeductions() + "]: ", sr.getDeductions()));
        sr.recalculate();

        System.out.println(salDAO.updateSalaryRecord(sr)
            ? "✔ Updated. New Net: ₹" + String.format("%,.2f", sr.getNetSalary())
            : "✘ Failed.");
    }

    private static void deleteSalary() {
        if (!auth.canWrite()) { System.out.println("Access denied."); return; }
        int id = readInt("Salary Record ID to delete: ");
        System.out.print("Confirm? (yes/no): ");
        if (!"yes".equalsIgnoreCase(sc.nextLine().trim())) { System.out.println("Cancelled."); return; }
        System.out.println(salDAO.deleteSalaryRecord(id) ? "✔ Deleted." : "✘ Failed.");
    }

    // ── REPORTS MENU ─────────────────────────────────────────────────────────

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n── Reports Dashboard ────────────────────────");
            System.out.println("  1. Dashboard Summary");
            System.out.println("  2. Full Employee Report");
            System.out.println("  3. Department Report");
            System.out.println("  4. Salary Report");
            System.out.println("  5. Top Earners");
            System.out.println("  6. Back");

            switch (readInt("Choice: ")) {
                case 1 -> reports.printDashboard();
                case 2 -> reports.printEmployeeReport();
                case 3 -> reports.printDepartmentReport();
                case 4 -> reports.printSalaryReport();
                case 5 -> reports.printTopEarners(readInt("How many top earners? "));
                case 6 -> back = true;
                default -> System.out.println("Invalid.");
            }
        }
    }

    // ── Input Helpers ────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     EMPLOYEE MANAGEMENT SYSTEM  v1.0         ║");
        System.out.println("║         Java + MySQL | JDBC                  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static String readStringOptional(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid amount."); }
        }
    }

    private static double readDoubleOptional(String prompt, double current) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isBlank()) return current;
        try { return Double.parseDouble(input); }
        catch (NumberFormatException e) { return current; }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return null;
            try { return LocalDate.parse(input); }
            catch (DateTimeParseException e) { System.out.println("Format: YYYY-MM-DD"); }
        }
    }
}
