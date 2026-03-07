package com.example.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

import java.util.*;

public class ViewAttendanceFragment extends Fragment {

    LinearLayout tableLayout;
    TextView summaryBox;
    TextView calculationBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_attendance, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        summaryBox = view.findViewById(R.id.summaryBox);
        calculationBox = view.findViewById(R.id.calculationBox);

        loadAttendance();

        return view;
    }

    private void loadAttendance() {

        SharedPreferences pref = getActivity().getSharedPreferences("attendance", 0);

        Map<String, ?> all = pref.getAll();

        int absentCount = 0;
        int halfCount = 0;

        StringBuilder absentDates = new StringBuilder();
        StringBuilder halfDates = new StringBuilder();

        for (Map.Entry<String, ?> entry : all.entrySet()) {

            String date = entry.getKey();
            String status = entry.getValue().toString();

            TextView row = new TextView(getContext());

            String letter = "";

            if (status.equals("Present")) {
                letter = "P";
            }
            else if (status.equals("Half Day")) {
                letter = "H";
                halfCount++;
                halfDates.append(date).append("\n");
            }
            else {
                letter = "A";
                absentCount++;
                absentDates.append(date).append("\n");
            }

            row.setText(date + " : " + letter);
            row.setPadding(10,10,10,10);

            tableLayout.addView(row);
        }

        summaryBox.setText(
                "Half Days:\n" + halfDates +
                "\nAbsent:\n" + absentDates
        );

        double halfValue = halfCount;
        double totalLeaves = absentCount + halfValue;

        calculationBox.setText(
                "Absent Days = " + absentCount +
                "\nHalf Days Value = " + halfValue +
                "\nTotal Leaves = " + totalLeaves
        );
    }
}
