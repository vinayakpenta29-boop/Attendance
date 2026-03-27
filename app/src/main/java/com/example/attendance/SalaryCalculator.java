package com.example.attendance;

import java.util.Calendar;

public class SalaryCalculator {

    public static class Result {

        public double baseSalary;
        public double perDaySalary;
        public double leaveDays;
        public double leaveDeduction;
        public double leaveBonus;
        public double schemeAmount;
        public double salaryWithBonus;
        public double tax;
        public double medical;
        public double pf;
        public double dabbaDeduction;
        public double totalDeductions;
        public double netSalary;
    }

    public static Result calculate(

            double grossSalary,
            double tax,
            double medical,
            int year,
            int month,
            double leaveDays,
            int dabbaUnit,
            boolean pfEnabled,
            double pfAmount,
            boolean schemeEnabled,
            double schemeAmount

    ) {

        Result r = new Result();

        int monthDays = getMonthDays(month, year);

        double perDaySalary = grossSalary / monthDays;

        double fifthMondayBonus = hasFifthMonday(year, month) ? perDaySalary : 0;

        double leaveDeduction = 0;
        double leaveBonus = 0;

        if (leaveDays == 4) {
            // No change
            leaveDeduction = 0;
            leaveBonus = 0;
        }
        else if (leaveDays < 4) {
            leaveBonus = (4 - leaveDays) * perDaySalary;
        }
        else {
            leaveDeduction = (leaveDays - 4) * perDaySalary;
        }

        double salaryWithBonus = grossSalary + leaveBonus;

        double salaryWithBonus = grossSalary + leaveBonus + fifthMondayBonus;

        double dabbaPerDay = 900.0 / monthDays;
        double dabbaDeduction = dabbaUnit * dabbaPerDay;

    
        double pf = pfEnabled ? pfAmount : 0;
        double scheme = schemeEnabled ? schemeAmount : 0;

        double totalDeductions =
                leaveDeduction +
                tax +
                medical +
                pf +
                dabbaDeduction;

        double netSalary =
                salaryWithBonus
                - totalDeductions
                + scheme;

        r.baseSalary = grossSalary;
        r.perDaySalary = perDaySalary;
        r.leaveDays = leaveDays;
        r.leaveDeduction = leaveDeduction;
        r.leaveBonus = leaveBonus;
        r.schemeAmount = scheme;
        r.salaryWithBonus = salaryWithBonus;
        r.tax = tax;
        r.medical = medical;
        r.pf = pf;
        r.dabbaDeduction = dabbaDeduction;
        r.totalDeductions = totalDeductions;
        r.netSalary = netSalary;

        return r;
    }

    private static int getMonthDays(int month, int year) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static boolean hasFifthMonday(int year, int month) {

    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month - 1, 1);

    int mondayCount = 0;
    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

    for (int day = 1; day <= daysInMonth; day++) {

        calendar.set(year, month - 1, day);

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            mondayCount++;
        }
    }

    return mondayCount >= 5;
}
}
