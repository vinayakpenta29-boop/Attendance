package com.example.attendance;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyAttendanceFragment extends Fragment {

    EditText dateBox;
    Spinner spinner;
    Spinner dabbaSpinner;
    Button addButton;

    Calendar calendar;

    SimpleDateFormat displayFormat;
    SimpleDateFormat storageFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // ✅ Enable menu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_attendance, container, false);

        dateBox = view.findViewById(R.id.dateBox);
        spinner = view.findViewById(R.id.spinner);
        dabbaSpinner = view.findViewById(R.id.dabbaSpinner);
        addButton = view.findViewById(R.id.addAttendance);

        calendar = Calendar.getInstance();

        displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        storageFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        dateBox.setText(displayFormat.format(calendar.getTime()));

        dateBox.setOnClickListener(v -> {

            DatePickerDialog picker = new DatePickerDialog(getContext(),
                    (view1, year, month, dayOfMonth) -> {

                        calendar.set(year, month, dayOfMonth);
                        dateBox.setText(displayFormat.format(calendar.getTime()));

                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            picker.getDatePicker().setMaxDate(System.currentTimeMillis());

            picker.show();
        });

        /* ATTENDANCE STATUS SPINNER */

        String[] options = {"Present", "Half Day", "Absent"};

        ArrayAdapter adapter = new ArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                options
        );

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String status = spinner.getSelectedItem().toString();

                if(status.equals("Half Day") || status.equals("Absent")){

                    dabbaSpinner.setEnabled(false);
                    dabbaSpinner.setSelection(4);
                    dabbaSpinner.setAlpha(0.4f);

                }
                else{

                    dabbaSpinner.setEnabled(true);
                    dabbaSpinner.setSelection(0);
                    dabbaSpinner.setAlpha(1f);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        /* DABBA STATUS SPINNER */

        String[] dabbaOptions = {"Select Dabba", "Dabba", "Ghari", "Late", "Absent"};

        ArrayAdapter dabbaAdapter = new ArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                dabbaOptions
        );

        dabbaSpinner.setAdapter(dabbaAdapter);
        dabbaSpinner.setSelection(0);

        addButton.setOnClickListener(v -> saveAttendance());

        return view;
    }

    /* ================= MENU ================= */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_daily_attendance, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_sales){
            showSalesDialog(); // ✅ Open Sales Dialog
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* ================= SALES FEATURE ================= */

    private void showSalesDialog() {

        Calendar today = Calendar.getInstance();
        final Calendar selectedDate = Calendar.getInstance(); // default today

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView dateText = new TextView(getContext());
        dateText.setText("Date: " + displayFormat.format(today.getTime()));
        dateText.setTextSize(16);

        Button pickDate = new Button(getContext());
        pickDate.setText("Select Date");

        EditText amountBox = new EditText(getContext());
        amountBox.setHint("Enter Sales Amount");
        amountBox.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        layout.addView(dateText);
        layout.addView(pickDate);
        layout.addView(amountBox);

        // 📅 Date Picker
        pickDate.setOnClickListener(v -> {

            DatePickerDialog picker = new DatePickerDialog(getContext(),
                    (view, year, month, day) -> {

                        selectedDate.set(year, month, day);
                        dateText.setText("Date: " + displayFormat.format(selectedDate.getTime()));

                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH));

            picker.show();
        });

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Add Sales")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {

                    String input = amountBox.getText().toString();

                    if(input.isEmpty()){
                        Toast.makeText(getContext(), "Enter amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newAmount = Integer.parseInt(input);

                    String key = storageFormat.format(selectedDate.getTime());

                    SharedPreferences pref =
                            getActivity().getSharedPreferences("sales_data", 0);

                    int oldAmount = pref.getInt(key, 0);

                    int total = oldAmount + newAmount; // ➕ ADD

                    pref.edit().putInt(key, total).apply();

                    Toast.makeText(getContext(),
                            "Saved Total: ₹" + total,
                            Toast.LENGTH_SHORT).show();

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ================= SAVE ATTENDANCE ================= */

    private void saveAttendance() {

        String status = spinner.getSelectedItem().toString();
        String dabbaStatus = dabbaSpinner.getSelectedItem().toString();

        String storageDate = storageFormat.format(calendar.getTime());

        SharedPreferences pref = getActivity().getSharedPreferences("attendance", 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(storageDate, status);

        if(status.equals("Half Day") || status.equals("Absent")){

            editor.putString(storageDate + "_dabba", "Absent");

        }
        else {

            if(dabbaStatus.equals("Select Dabba")){
                editor.remove(storageDate + "_dabba");
            }
            else{
                editor.putString(storageDate + "_dabba", dabbaStatus);
            }
        }

        editor.apply();

        getActivity().getSupportFragmentManager()
                .setFragmentResult("refresh", new Bundle());

        spinner.setSelection(0);
        dabbaSpinner.setSelection(0);

        Toast.makeText(getContext(), "Attendance Added", Toast.LENGTH_SHORT).show();
    }
}
