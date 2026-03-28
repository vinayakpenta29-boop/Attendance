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

    LinearLayout salaryContainer;
    TextView netSalaryText;

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
        getParentFragmentManager().setFragmentResultListener(
                "month_changed",
                this,
                (key, bundle) -> calculateSalary()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_salary, container, false);

        salaryContainer = view.findViewById(R.id.salaryContainer);
        netSalaryText = view.findViewById(R.id.netSalaryText);

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
            return true;
        }
        if (item.getItemId() == R.id.clearData) {
            showPasswordDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        int dabbaUnits = getDabbaUnitsFromAttendance();

        // 🔥 GET SELECTED MONTH FROM VIEW ATTENDANCE
        SharedPreferences pref =
                getActivity().getSharedPreferences("selected_month", 0);

        int year = pref.getInt("year", Calendar.getInstance().get(Calendar.YEAR));
        int month = pref.getInt("month", Calendar.getInstance().get(Calendar.MONTH) + 1);

        SalaryCalculator.Result result =
                SalaryCalculator.calculate(
                        monthlySalary,
                        tax,
                        medicine,
                        year,
                        month,
                        leaveDays,
                        dabbaUnits,
                        pfEnabled,
                        pfAmount,
                        schemeEnabled,
                        schemeAmount
                );

        salaryContainer.removeAllViews();

        /* helper function */
        addRow("Base Monthly Salary", "₹" + result.baseSalary);
        addDivider();

        addRow("Per Day Salary", "₹" + round(result.perDaySalary));
        addDivider();

        /* ATTENDANCE */
        

        addRow("Leave Days", String.valueOf(result.leaveDays));
        addDivider();

        /* LEAVE DISPLAY LOGIC */

        if(result.leaveDays == 4){
            addRow("Leave Adjustment", "No Leave Adjustment");
        }
        else if(result.leaveDays < 4){
            addRow("Bonus", "₹" + round(result.leaveBonus));
        }
        else{
            addRow("Leave Deduction", "₹" + round(result.leaveDeduction));
        }

        addDivider();

        double fifthMondayBonus =
                (SalaryCalculator.hasFifthMonday(year, month)) ? result.perDaySalary : 0;

        if(fifthMondayBonus > 0){
            addRow("5th Monday Bonus", "₹" + round(fifthMondayBonus));
            addDivider();
        }

        /* DEDUCTIONS */
        

        addRow("Tax", "₹" + result.tax);
        addDivider();

        addRow("Medical", "₹" + result.medical);
        addDivider();

        addRow("PF", "₹" + result.pf);
        addDivider();

        addRow("Dabba Deduction", "₹" + round(result.dabbaDeduction));
        addDivider();

        addRow("Total Deduction", "₹" + round(result.totalDeductions));

        /* NET SALARY BOX */
        netSalaryText.setText("₹" + round(result.netSalary));
    }

    private void addRow(String label, String value){

    LinearLayout row = new LinearLayout(getContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(8,12,8,12);

    TextView left = new TextView(getContext());
    left.setText(label);
    left.setTextSize(16);
    left.setTypeface(null, android.graphics.Typeface.BOLD);
    left.setTextColor(0xFF000000);

    LinearLayout.LayoutParams lp1 =
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
    left.setLayoutParams(lp1);

    TextView right = new TextView(getContext());
    right.setText(value);
    right.setTextSize(16);
    right.setTypeface(null, android.graphics.Typeface.BOLD);
    right.setTextColor(0xFF000000);
    right.setGravity(Gravity.END);

    LinearLayout.LayoutParams lp2 =
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
    right.setLayoutParams(lp2);

    row.addView(left);
    row.addView(right);

    salaryContainer.addView(row);
}

    private void addDivider(){

    View divider = new View(getContext());

    LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            );

    params.setMargins(0,4,0,4);

    divider.setLayoutParams(params);
    divider.setBackgroundColor(0xFFDDDDDD);

    salaryContainer.addView(divider);
}

    private void addHeader(String text){

    TextView header = new TextView(getContext());
    header.setText(text);
    header.setTextSize(14);
    header.setTypeface(null, android.graphics.Typeface.BOLD);
    header.setTextColor(0xFF990F4B);
    header.setPadding(0,16,0,8);

    salaryContainer.addView(header);

    addDivider();
}

    /* GET REAL ATTENDANCE FROM SHARED PREFERENCES */
    private double getLeaveDaysFromAttendance() {

        SharedPreferences pref =
                getActivity().getSharedPreferences("attendance_summary", 0);

        int absent = pref.getInt("absentCount", 0);
        int half = pref.getInt("halfCount", 0);

        return absent + (half * 0.5);
    }

    private int getDabbaUnitsFromAttendance() {

        SharedPreferences pref =
                getActivity().getSharedPreferences("attendance_summary", 0);

        return pref.getInt("dabbaCount", 0);
    }

    private void showPasswordDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setTitle("Enter Password");

    final EditText input = new EditText(getContext());
    input.setHint("Password");
    input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
            android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    input.setPadding(40, 20, 40, 20);

    builder.setView(input);

    builder.setPositiveButton("Clear", (dialog, which) -> {

        String entered = input.getText().toString();

        if (entered.equals("1234")) {

            clearAllData();

            Toast.makeText(getContext(), "All Data Cleared", Toast.LENGTH_SHORT).show();

            calculateSalary(); // 🔥 refresh UI

        } else {

            Toast.makeText(getContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
        }
    });

    builder.setNegativeButton("Cancel", null);

    builder.show();
}

private void clearAllData() {

    // Attendance data
    SharedPreferences attendance =
            getActivity().getSharedPreferences("attendance", 0);
    attendance.edit().clear().apply();

    // Summary data
    SharedPreferences summary =
            getActivity().getSharedPreferences("attendance_summary", 0);
    summary.edit().clear().apply();

    // Salary inputs (⚠ FIX NAME)
    SharedPreferences salary =
            getActivity().getSharedPreferences("salary_inputs", 0);
    salary.edit().clear().apply();

    // Reset UI values
    monthlySalary = 0;
    tax = 0;
    medicine = 0;
    pfAmount = 0;
    schemeAmount = 0;

    pfEnabled = false;
    schemeEnabled = false;
}
}
