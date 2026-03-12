package com.example.attendance;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private void saveAttendance() {

        String status = spinner.getSelectedItem().toString();
        String dabbaStatus = dabbaSpinner.getSelectedItem().toString();

        String storageDate = storageFormat.format(calendar.getTime());

        SharedPreferences pref = getActivity().getSharedPreferences("attendance", 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(storageDate, status);
        if(!dabbaStatus.equals("Select Dabba")){
            editor.putString(storageDate + "_dabba", dabbaStatus);
        }

        editor.apply();

        spinner.setSelection(0);
        dabbaSpinner.setSelection(0);

        Toast.makeText(getContext(), "Attendance Added", Toast.LENGTH_SHORT).show();
    }
}
