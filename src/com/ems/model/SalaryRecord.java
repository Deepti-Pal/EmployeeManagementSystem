package com.ems.model;

import java.time.LocalDate;

/**
 * SalaryRecord - Represents a monthly salary entry (maps to `salary_records` table).
 */
public class SalaryRecord {

    private int    salaryId;
    private int    empId;
    private String empName;       // joined from employees
    private String empCode;       // joined from employees
    private double basicSalary;
    private double hra;
    private double da;
    private double allowances;
    private double deductions;
    private double netSalary;     // computed: basic + hra + da + allowances - deductions
    private LocalDate effectiveFrom;
    private String payMonth;

    // ── Constructors ────────────────────────────────────────────────────────

    public SalaryRecord() {}

    public SalaryRecord(int empId, double basicSalary, double hra, double da,
                        double allowances, double deductions,
                        LocalDate effectiveFrom, String payMonth) {
        this.empId         = empId;
        this.basicSalary   = basicSalary;
        this.hra           = hra;
        this.da            = da;
        this.allowances    = allowances;
        this.deductions    = deductions;
        this.effectiveFrom = effectiveFrom;
        this.payMonth      = payMonth;
        this.netSalary     = basicSalary + hra + da + allowances - deductions;
    }

    /** Re-computes netSalary from current field values. */
    public void recalculate() {
        this.netSalary = basicSalary + hra + da + allowances - deductions;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public int    getSalaryId()    { return salaryId; }
    public void   setSalaryId(int v) { this.salaryId = v; }

    public int    getEmpId()       { return empId; }
    public void   setEmpId(int v)  { this.empId = v; }

    public String getEmpName()     { return empName; }
    public void   setEmpName(String v) { this.empName = v; }

    public String getEmpCode()     { return empCode; }
    public void   setEmpCode(String v) { this.empCode = v; }

    public double getBasicSalary() { return basicSalary; }
    public void   setBasicSalary(double v) { this.basicSalary = v; }

    public double getHra()         { return hra; }
    public void   setHra(double v) { this.hra = v; }

    public double getDa()          { return da; }
    public void   setDa(double v)  { this.da = v; }

    public double getAllowances()   { return allowances; }
    public void   setAllowances(double v) { this.allowances = v; }

    public double getDeductions()  { return deductions; }
    public void   setDeductions(double v) { this.deductions = v; }

    public double getNetSalary()   { return netSalary; }
    public void   setNetSalary(double v) { this.netSalary = v; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate v) { this.effectiveFrom = v; }

    public String getPayMonth()    { return payMonth; }
    public void   setPayMonth(String v) { this.payMonth = v; }

    @Override
    public String toString() {
        return String.format(
            "SalaryRecord{emp='%s', month='%s', basic=%.2f, net=%.2f}",
            empCode, payMonth, basicSalary, netSalary
        );
    }
}
