package com.example.attendance;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

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

        pfAmountBox.setVisibility(View.GONE);
        schemeAmountBox.setVisibility(View.GONE);

        pfGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.pfYes) {
                pfAmountBox.setVisibility(View.VISIBLE);
                pfEnabled = true;
            } else {
                pfAmountBox.setVisibility(View.GONE);
                pfEnabled = false;
            }
        });

        schemeGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.schemeYes) {
                schemeAmountBox.setVisibility(View.VISIBLE);
                schemeEnabled = true;
            } else {
                schemeAmountBox.setVisibility(View.GONE);
                schemeEnabled = false;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        saveBtn.setOnClickListener(v -> {

            monthlySalary = Double.parseDouble(monthlySalaryBox.getText().toString());
            tax = Double.parseDouble(taxBox.getText().toString());
            medicine = Double.parseDouble(medicineBox.getText().toString());

            if (pfEnabled)
                pfAmount = Double.parseDouble(pfAmountBox.getText().toString());

            if (schemeEnabled)
                schemeAmount = Double.parseDouble(schemeAmountBox.getText().toString());

            double totalDeduction = tax + medicine + pfAmount + schemeAmount;

            double finalSalary = monthlySalary - totalDeduction;

            salaryResult.setText(
                    "Monthly Salary : ₹" + monthlySalary +
                    "\nTotal Deduction : ₹" + totalDeduction +
                    "\nFinal Salary : ₹" + finalSalary
            );

            dialog.dismiss();

        });

        dialog.show();
    }
}
