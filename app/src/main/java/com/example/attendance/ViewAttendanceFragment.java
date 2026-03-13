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
    TableLayout payrollTable;

    int absentCount = 0;
    int halfCount = 0;
    int dabbaCount = 0;

    int dabbaD = 0;
    int dabbaG = 0;
    int dabbaL = 0;
    int dabbaA = 0;

    SimpleDateFormat keyFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    SimpleDateFormat displayFormat =
            new SimpleDateFormat("dd MMM", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_attendance, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        summaryTable = view.findViewById(R.id.summaryTable);
        payrollTable = view.findViewById(R.id.payrollTable);

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
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setBackgroundColor(0xFFE9EEF5);

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
                cell.setPadding(16,16,16,16);
                cell.setBackgroundResource(R.drawable.calendar_cell_bg);

                TableRow.LayoutParams params =
                        new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1f);
                params.setMargins(1,1,1,1);
                cell.setLayoutParams(params);

                TextView dayNumber = new TextView(getContext());
                dayNumber.setTextSize(13);
                dayNumber.setGravity(Gravity.CENTER);
                dayNumber.setTextColor(0xFF555555);

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
                    String displayDate = displayFormat.format(tempCal.getTime());

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
                        halfDates.add(displayDate);

                    }
                    else if(status.equals("Absent")){

                        letter = "A";
                        absentCount++;
                        absentDates.add(displayDate);

                    }

                    if(dabba.equals("Dabba")){
                        dabbaLetter = "D";
                        dabbaDates.add(displayDate);
                        dabbaCount++;
                        dabbaD++;
                    }
                    else if(dabba.equals("Ghari")){
                        dabbaLetter = "G";
                        dabbaDates.add(displayDate);
                        dabbaCount++;
                        dabbaG++;
                    }
                    else if(dabba.equals("Late")){
                        dabbaLetter = "L";
                        dabbaDates.add(displayDate);
                        dabbaCount++;
                        dabbaL++;
                    }
                    else if(dabba.equals("Absent")){
                        dabbaLetter = "A";
                        dabbaDates.add(displayDate);
                        dabbaCount++;
                        dabbaA++;
                    }

                    statusText.setText(letter + "/" + dabbaLetter);
                    statusText.setTypeface(null, android.graphics.Typeface.BOLD);

                    if(letter.equals("P"))
                        statusText.setTextColor(0xFF2E7D32);   // Green

                    else if(letter.equals("H"))
                        statusText.setTextColor(0xFFF57C00);   // Orange

                    else if(letter.equals("A"))
                        statusText.setTextColor(0xFFC62828);   // Red

                    dayCounter++;
                }

                cell.addView(dayNumber);
                cell.addView(statusText);

                row.addView(cell);
            }

            tableLayout.addView(row);
        }

        summaryTable.removeAllViews();

        /* ===== HEADER ROW ===== */

        TableRow header = new TableRow(getContext());
        header.setBackgroundColor(0xFF3F51B5);

        TextView h1 = new TextView(getContext());
        TextView h2 = new TextView(getContext());
        TextView h3 = new TextView(getContext());

        h1.setText("Half Days");
        h2.setText("Absent");
        h3.setText("Dabba");

        TextView[] headers = {h1,h2,h3};

        for(TextView h : headers){

            h.setPadding(20,20,20,20);
            h.setGravity(Gravity.CENTER);
            h.setTextSize(15);
            h.setTypeface(null, android.graphics.Typeface.BOLD);
            h.setTextColor(0xFFFFFFFF);

            header.addView(h);
        }

        summaryTable.addView(header);


        /* ===== MAX ROW COUNT ===== */

        int max = Math.max(halfDates.size(),
                Math.max(absentDates.size(), dabbaDates.size()));


        /* ===== DATA ROWS ===== */

        for(int i=0;i<max;i++){

            TableRow row = new TableRow(getContext());

            if(i % 2 == 0)
                row.setBackgroundColor(0xFFF7F9FC);
            else
                row.setBackgroundColor(0xFFFFFFFF);

            TextView c1 = new TextView(getContext());
            TextView c2 = new TextView(getContext());
            TextView c3 = new TextView(getContext());

            TextView[] cells = {c1,c2,c3};

            for(TextView c : cells){

                c.setPadding(18,18,18,18);
                c.setGravity(Gravity.CENTER);
                c.setTextSize(14);
                c.setBackgroundResource(R.drawable.history_cell_bg);

                TableRow.LayoutParams params =
                        new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1f);
                params.setMargins(6,6,6,6);
                c.setLayoutParams(params);
            }

            if(i < halfDates.size()){
                c1.setText(halfDates.get(i));
                c1.setTextColor(0xFFF57C00); // Orange
            }

            if(i < absentDates.size()){
                c2.setText(absentDates.get(i));
                c2.setTextColor(0xFFC62828); // Red
            }

            if(i < dabbaDates.size()){
                c3.setText(dabbaDates.get(i));
                c3.setTextColor(0xFF1565C0); // Blue
            }

            row.addView(c1);
            row.addView(c2);
            row.addView(c3);

            summaryTable.addView(row);
        }

        double halfValue = halfCount * 0.5;
        double totalLeaves = absentCount + halfValue;

        payrollTable.removeAllViews();

        /* HEADER */

        TableRow header = new TableRow(getContext());
        header.setBackgroundColor(0xFF009688);

        String[] titles = {"Category","Value"};

        for(String t : titles){

            TextView tv = new TextView(getContext());
            tv.setText(t);
            tv.setPadding(20,20,20,20);
            tv.setTextColor(0xFFFFFFFF);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);

            header.addView(tv);
        }

        payrollTable.addView(header);


        /* DATA ROWS */

        String[][] data = {

                {"Absent Days", String.valueOf(absentCount)},
                {"Half Days", String.valueOf(halfValue)},
                {"Total Leaves", String.valueOf(totalLeaves)},

                {"Dabba (D)", String.valueOf(dabbaD)},
                {"Ghari (G)", String.valueOf(dabbaG)},
                {"Late (L)", String.valueOf(dabbaL)},
                {"Absent (A)", String.valueOf(dabbaA)},

                {"Total Dabba", String.valueOf(dabbaCount)}
        };

        for(int i=0;i<data.length;i++){

            TableRow row = new TableRow(getContext());

            if(i % 2 == 0)
                row.setBackgroundColor(0xFFFFFFFF);
            else
                row.setBackgroundColor(0xFFF7F9FC);

            for(int j=0;j<2;j++){

                TextView cell = new TextView(getContext());

                cell.setText(data[i][j]);
                cell.setPadding(18,18,18,18);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(14);
                cell.setBackgroundResource(R.drawable.history_cell_bg);

                TableRow.LayoutParams params =
                        new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1f);
                params.setMargins(6,6,6,6);
                cell.setLayoutParams(params);

                row.addView(cell);
            }

             payrollTable.addView(row);
        }

        SharedPreferences.Editor editor =
                getActivity().getSharedPreferences("attendance_summary", 0).edit();

        editor.putInt("absentCount", absentCount);
        editor.putInt("halfCount", halfCount);
        editor.putFloat("totalLeaves", (float) totalLeaves);
        editor.putInt("dabbaCount", dabbaCount);

        editor.apply();
    }
}
