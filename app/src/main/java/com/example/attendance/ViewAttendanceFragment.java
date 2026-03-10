package com.example.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ViewAttendanceFragment extends Fragment {

    TableLayout tableLayout;
    TextView summaryBox;
    TextView calculationBox;

    int absentCount = 0;
    int halfCount = 0;

    SimpleDateFormat keyFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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

        absentCount = 0;
        halfCount = 0;

        StringBuilder absentDates = new StringBuilder();
        StringBuilder halfDates = new StringBuilder();

        tableLayout.removeAllViews();

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int dayCounter = 1;

        /* HEADER */

        TableRow headerRow = new TableRow(getContext());

        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

        for(String d : days){

            TextView tv = new TextView(getContext());
            tv.setText(d);
            tv.setPadding(20,20,20,20);
            tv.setGravity(Gravity.CENTER);

            headerRow.addView(tv);
        }

        tableLayout.addView(headerRow);

        /* CALENDAR */

        while(dayCounter <= daysInMonth){

            TableRow row = new TableRow(getContext());

            for(int i=0;i<7;i++){

                TextView cell = new TextView(getContext());
                cell.setPadding(20,40,20,40);
                cell.setGravity(Gravity.CENTER);

                if(dayCounter == 1 && i < firstDayOfWeek){

                    cell.setText("");

                }
                else if(dayCounter <= daysInMonth){

                    Calendar tempCal = Calendar.getInstance();
                    tempCal.set(year, month, dayCounter);

                    String dateKey = keyFormat.format(tempCal.getTime());

                    String status = pref.getString(dateKey,"");

                    String letter = "";

                    if(status.equals("Present")){

                        letter = "P";

                    }
                    else if(status.equals("Half Day")){

                        letter = "H";
                        halfCount++;
                        halfDates.append(dateKey).append("\n");

                    }
                    else if(status.equals("Absent")){

                        letter = "A";
                        absentCount++;
                        absentDates.append(dateKey).append("\n");

                    }

                    cell.setText(letter);

                    dayCounter++;
                }

                row.addView(cell);
            }

            tableLayout.addView(row);
        }

        summaryBox.setText(
                "Half Days:\n" + halfDates +
                "\nAbsent:\n" + absentDates
        );

        double halfValue = halfCount * 0.5;
        double totalLeaves = absentCount + halfValue;

        calculationBox.setText(
                "Absent Days = " + absentCount +
                "\nHalf Days Value = " + halfValue +
                "\nTotal Leaves = " + totalLeaves
        );

        SharedPreferences.Editor editor =
                getActivity().getSharedPreferences("attendance_summary", 0).edit();

        editor.putInt("absentCount", absentCount);
        editor.putInt("halfCount", halfCount);
        editor.putFloat("totalLeaves", (float) totalLeaves);

        editor.apply();
    }
}
