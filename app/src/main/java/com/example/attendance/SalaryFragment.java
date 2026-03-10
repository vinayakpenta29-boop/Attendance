package com.example.attendance;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

import com.example.attendance.SalaryCalculator;

import java.util.Calendar;

public class SalaryFragment extends Fragment {

    TextView salaryResult;

    double monthlySalary = 0;
    double tax = 0;
    double medicine = 0;
    double pfAmount = 0;
    double schemeAmount = 0;

    boolean pfEnabled = false;
    boolean schemeEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_salary, container, false);

        salaryResult = view.findViewById(R.id.salaryResult);

        SharedPreferences pref =
        getActivity().getSharedPreferences("salary_inputs", 0);

        monthlySalary = pref.getFloat("monthlySalary", 0);
        tax = pref.getFloat("tax", 0);
        medicine = pref.getFloat("medicine", 0);
        pfAmount = pref.getFloat("pfAmount", 0);
        schemeAmount = pref.getFloat("schemeAmount", 0);

        pfEnabled = pref.getBoolean("pfEnabled", false);
        schemeEnabled = pref.getBoolean("schemeEnabled", false);
        
        calculateSalary();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.salary_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.salaryInputs) {
            showSalaryInputPopup();
        }

        return true;
    }

    private void showSalaryInputPopup() {

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.popup_salary_inputs, null);

        EditText monthlySalaryBox = dialogView.findViewById(R.id.monthlySalary);
        EditText taxBox = dialogView.findViewById(R.id.tax);
        EditText medicineBox = dialogView.findViewById(R.id.medicine);
        EditText pfAmountBox = dialogView.findViewById(R.id.pfAmount);
        EditText schemeAmountBox = dialogView.findViewById(R.id.schemeAmount);

        RadioGroup pfGroup = dialogView.findViewById(R.id.pfGroup);
        RadioGroup schemeGroup = dialogView.findViewById(R.id.schemeGroup);

        Button saveBtn = dialogView.findViewById(R.id.saveInputs);

        /* Load saved values into fields */

        monthlySalaryBox.setText(monthlySalary == 0 ? "" : String.valueOf(monthlySalary));
        taxBox.setText(tax == 0 ? "" : String.valueOf(tax));
        medicineBox.setText(medicine == 0 ? "" : String.valueOf(medicine));

        if(pfEnabled){
            pfGroup.check(R.id.pfYes);
            pfAmountBox.setVisibility(View.VISIBLE);
            pfAmountBox.setText(String.valueOf(pfAmount));
        }

        if(schemeEnabled){
            schemeGroup.check(R.id.schemeYes);
            schemeAmountBox.setVisibility(View.VISIBLE);
            schemeAmountBox.setText(String.valueOf(schemeAmount));
        }

        pfAmountBox.setVisibility(View.GONE);
        schemeAmountBox.setVisibility(View.GONE);

        pfGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.pfYes) {
                pfAmountBox.setVisibility(View.VISIBLE);
                pfEnabled = true;
            } else {
                pfAmountBox.setVisibility(View.GONE);
                pfEnabled = false;
                pfAmount = 0;
            }
        });

        schemeGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.schemeYes) {
                schemeAmountBox.setVisibility(View.VISIBLE);
                schemeEnabled = true;
            } else {
                schemeAmountBox.setVisibility(View.GONE);
                schemeEnabled = false;
                schemeAmount = 0;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveBtn.setOnClickListener(v -> {

            monthlySalary = parseDouble(monthlySalaryBox);
            tax = parseDouble(taxBox);
            medicine = parseDouble(medicineBox);

            if (pfEnabled)
                pfAmount = parseDouble(pfAmountBox);

            if (schemeEnabled)
                schemeAmount = parseDouble(schemeAmountBox);

            /* SAVE INPUTS */

            SharedPreferences.Editor editor =
                    getActivity().getSharedPreferences("salary_inputs",0).edit();

            editor.putFloat("monthlySalary",(float) monthlySalary);
            editor.putFloat("tax",(float) tax);
            editor.putFloat("medicine",(float) medicine);
            editor.putFloat("pfAmount",(float) pfAmount);
            editor.putFloat("schemeAmount",(float) schemeAmount);

            editor.putBoolean("pfEnabled",pfEnabled);
            editor.putBoolean("schemeEnabled",schemeEnabled);

            editor.apply();
            calculateSalary();
            dialog.dismiss();

        });

        dialog.show();
    }

    private double parseDouble(EditText box) {

        try {
            return Double.parseDouble(box.getText().toString());
        } catch (Exception e) {
            return 0;
        }

    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;

    }

    private void calculateSalary() {

        /* GET REAL ATTENDANCE DATA */
        double leaveDays = getLeaveDaysFromAttendance();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        SalaryCalculator.Result result =
                SalaryCalculator.calculate(
                        monthlySalary,
                        tax,
                        medicine,
                        year,
                        month,
                        leaveDays,
                        pfEnabled,
                        pfAmount,
                        schemeEnabled,
                        schemeAmount
                );

        salaryResult.setText(

                "Base Salary : ₹" + result.baseSalary +
                "\nPer Day Salary : ₹" + round(result.perDaySalary) +
                "\nLeave Days : " + result.leaveDays +
                "\nLeave Deduction : ₹" + round(result.leaveDeduction) +
                "\nBonus : ₹" + round(result.leaveBonus) +
                "\nTax : ₹" + result.tax +
                "\nMedical : ₹" + result.medical +
                "\nPF : ₹" + result.pf +
                "\nTotal Deduction : ₹" + round(result.totalDeductions) +
                "\n\nNet Salary : ₹" + round(result.netSalary)

        );
    }

    /* GET REAL ATTENDANCE FROM SHARED PREFERENCES */
    private double getLeaveDaysFromAttendance() {

        SharedPreferences pref =
                getActivity().getSharedPreferences("attendance_summary", 0);

        int absent = pref.getInt("absentCount", 0);
        int half = pref.getInt("halfCount", 0);

        return absent + (half * 0.5);
    }
}
