package com.example.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;

public class ViewAttendanceFragment extends Fragment {

    TableLayout tableLayout;
    TableLayout summaryTable;
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
        summaryTable = view.findViewById(R.id.summaryTable);
        calculationBox = view.findViewById(R.id.calculationBox);

        loadAttendance();

        return view;
    }

    private void loadAttendance() {

        SharedPreferences pref = getActivity().getSharedPreferences("attendance", 0);

        absentCount = 0;
        halfCount = 0;

        ArrayList<String> halfDates = new ArrayList<>();
        ArrayList<String> absentDates = new ArrayList<>();
        ArrayList<String> dabbaDates = new ArrayList<>();

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
            tv.setTextSize(16);

            headerRow.addView(tv);
        }

        tableLayout.addView(headerRow);

        /* CALENDAR GRID */

        while(dayCounter <= daysInMonth){

            TableRow row = new TableRow(getContext());

            for(int i=0;i<7;i++){

                LinearLayout cell = new LinearLayout(getContext());
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(20,20,20,20);

                TextView dayNumber = new TextView(getContext());
                dayNumber.setTextSize(14);
                dayNumber.setGravity(Gravity.CENTER);

                TextView statusText = new TextView(getContext());
                statusText.setTextSize(18);
                statusText.setGravity(Gravity.CENTER);

                if(dayCounter == 1 && i < firstDayOfWeek){

                    dayNumber.setText("");
                    statusText.setText("");

                }
                else if(dayCounter <= daysInMonth){

                    dayNumber.setText(String.valueOf(dayCounter));

                    Calendar tempCal = Calendar.getInstance();
                    tempCal.set(year, month, dayCounter);

                    String dateKey = keyFormat.format(tempCal.getTime());

                    String status = pref.getString(dateKey,"");
                    
                    String dabba = pref.getString(dateKey + "_dabba","");
                    
                    String letter = "";

                    String dabbaLetter = "-";

                    if(status.equals("Present")){

                        letter = "P";

                    }
                    else if(status.equals("Half Day")){

                        letter = "H";
                        halfCount++;
                        halfDates.add(dateKey);

                    }
                    else if(status.equals("Absent")){

                        letter = "A";
                        absentCount++;
                        absentDates.add(dateKey);

                    }

                    if(dabba.equals("Dabba")){
                        dabbaLetter = "D";
                        dabbaDates.add(dateKey);
                    }
                    else if(dabba.equals("Ghari")){
                        dabbaLetter = "G";
                        dabbaDates.add(dateKey);
                    }
                    else if(dabba.equals("Late")){
                        dabbaLetter = "L";
                        dabbaDates.add(dateKey);
                    }

                    statusText.setText(letter + "/" + dabbaLetter);

                    dayCounter++;
                }

                cell.addView(dayNumber);
                cell.addView(statusText);

                row.addView(cell);
            }

            tableLayout.addView(row);
        }

        summaryTable.removeAllViews();

        /* HEADER ROW */

        TableRow header = new TableRow(getContext());

        TextView h1 = new TextView(getContext());
        TextView h2 = new TextView(getContext());
        TextView h3 = new TextView(getContext());

        h1.setText("H-Days");
        h2.setText("Absents");
        h3.setText("Dabbas");

        h1.setPadding(20,20,20,20);
        h2.setPadding(20,20,20,20);
        h3.setPadding(20,20,20,20);

        header.addView(h1);
        header.addView(h2);
        header.addView(h3);

        summaryTable.addView(header);

        /* MAX ROW COUNT */

        int max = Math.max(halfDates.size(),
                  Math.max(absentDates.size(), dabbaDates.size()));

        /* DATA ROWS */

        for(int i=0;i<max;i++){

            TableRow row = new TableRow(getContext());

            TextView c1 = new TextView(getContext());
            TextView c2 = new TextView(getContext());
            TextView c3 = new TextView(getContext());

            c1.setPadding(20,20,20,20);
            c2.setPadding(20,20,20,20);
            c3.setPadding(20,20,20,20);

            if(i < halfDates.size())
                c1.setText(halfDates.get(i));

            if(i < absentDates.size())
                c2.setText(absentDates.get(i));

            if(i < dabbaDates.size())
                c3.setText(dabbaDates.get(i));

            row.addView(c1);
            row.addView(c2);
            row.addView(c3);

            summaryTable.addView(row);
        }

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
