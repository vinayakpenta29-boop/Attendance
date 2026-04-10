package com.example.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.*;
import android.widget.*;
import android.app.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;

public class ViewAttendanceFragment extends Fragment {

    ImageView prevMonth, nextMonth;
    TextView monthText;
    Calendar currentCalendar;
    
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
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_view_attendance, container, false);

        prevMonth = view.findViewById(R.id.prevMonth);
        nextMonth = view.findViewById(R.id.nextMonth);
        monthText = view.findViewById(R.id.monthText);

        currentCalendar = Calendar.getInstance();

        getParentFragmentManager().setFragmentResultListener(
                "refresh", this, (key, bundle) -> updateMonth()
        );
        
        tableLayout = view.findViewById(R.id.tableLayout);
        summaryTable = view.findViewById(R.id.summaryTable);
        payrollTable = view.findViewById(R.id.payrollTable);

        updateMonth();

        prevMonth.setOnClickListener(v -> {
        currentCalendar.add(Calendar.MONTH, -1);
            updateMonth();
        });

        nextMonth.setOnClickListener(v -> {
        currentCalendar.add(Calendar.MONTH, 1);
            updateMonth();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentCalendar != null) {
            updateMonth(); // 🔥 reload everything
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_attendance, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_holidays){
            showHolidayDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMonth() {

        SimpleDateFormat monthFormat =
                new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        monthText.setText(monthFormat.format(currentCalendar.getTime()));
        SharedPreferences.Editor editor =
                getActivity().getSharedPreferences("selected_month", 0).edit();

        editor.putInt("year", currentCalendar.get(Calendar.YEAR));
        editor.putInt("month", currentCalendar.get(Calendar.MONTH) + 1);

        editor.apply();

        loadAttendance();

        // 🔥 NOTIFY SALARY TAB TO REFRESH
        Bundle bundle = new Bundle();
        getParentFragmentManager().setFragmentResult("month_changed", bundle);
    }

    private void loadAttendance() {

        SharedPreferences pref = getActivity().getSharedPreferences("attendance", 0);

        absentCount = 0;
        halfCount = 0;
        dabbaCount = 0;
        dabbaD = 0;
        dabbaG = 0;
        dabbaL = 0;
        dabbaA = 0;

        ArrayList<String> halfDates = new ArrayList<>();
        ArrayList<String> absentDates = new ArrayList<>();
        ArrayList<String> dabbaDates = new ArrayList<>();

        tableLayout.removeAllViews();

        Calendar calendar = (Calendar) currentCalendar.clone();

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
                if(i == 0){
                cell.setBackgroundResource(R.drawable.sunday_cell_bg);
                } else {
                cell.setBackgroundResource(R.drawable.calendar_cell_bg);
                }

                TableRow.LayoutParams params =
                        new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1f);
                params.setMargins(1,1,1,1);
                cell.setLayoutParams(params);

                TextView dayNumber = new TextView(getContext());
                dayNumber.setTextSize(13);
                dayNumber.setGravity(Gravity.CENTER);
                if(i == 0){
                    dayNumber.setTextColor(0xFFC62828); // 🔴 Sunday = Red
                    dayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
                    dayNumber.setBackgroundResource(R.drawable.holiday_date_bg);
                }else{
                    dayNumber.setTextColor(0xFF555555); // Normal
                }

                TextView statusText = new TextView(getContext());
                statusText.setTextSize(16);
                statusText.setGravity(Gravity.CENTER);
                statusText.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView dabbaText = new TextView(getContext());
                dabbaText.setTextSize(16);
                dabbaText.setTextColor(0xFF1565C0);
                dabbaText.setGravity(Gravity.CENTER);
                dabbaText.setTypeface(null, android.graphics.Typeface.BOLD);

                View divider = new View(getContext());

                LinearLayout.LayoutParams dividerParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2
                        );

                dividerParams.setMargins(4,4,4,4);

                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(0xFFDDDDDD);
                
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

                    SharedPreferences holidayPref =
                            getActivity().getSharedPreferences("holidays", 0);

                    String holidayName = holidayPref.getString(dateKey, null);

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

                    statusText.setText(letter);
                    dabbaText.setText(dabbaLetter);

                    if(letter.equals("P")){
                        statusText.setTextColor(0xFF2E7D32);
                        cell.setBackgroundResource(R.drawable.present_bg);
                        }

                    else if(letter.equals("H")){
                        statusText.setTextColor(0xFFF57C00);   // Orange
                        cell.setBackgroundResource(R.drawable.half_day_bg);
                    }

                    else if(letter.equals("A")){
                        statusText.setTextColor(0xFFC62828);   // Red
                        cell.setBackgroundResource(R.drawable.absent_cell_bg);
                    }

                    else if(holidayName != null){
                        cell.setBackgroundResource(R.drawable.sunday_cell_bg);
                            cell.setOnClickListener(v -> {
                                new android.app.AlertDialog.Builder(getContext())
                                        .setTitle("Holiday")
                                        .setMessage(holidayName)
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                    }

                    dayCounter++;
                }

                cell.addView(dayNumber);
                cell.addView(statusText);
                cell.addView(divider);
                cell.addView(dabbaText);

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

        TableRow payrollHeader = new TableRow(getContext());
        payrollHeader.setBackgroundColor(0xFF009688);
        String[] titles = {"Category","Value"};

        for(String t : titles){

            TextView tv = new TextView(getContext());
            tv.setText(t);
            tv.setPadding(20,20,20,20);
            tv.setTextColor(0xFFFFFFFF);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);

            payrollHeader.addView(tv);
        }

        payrollTable.addView(payrollHeader);


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

                /* ===== SPECIAL COLOR ROWS ===== */

                // Total Leaves Row (index 2)
                if(i == 2){
                    cell.setBackgroundResource(R.drawable.total_leaves_bg);
                    cell.setTextColor(0xFFB71C1C);           // Dark Red
                    cell.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                // Total Dabba Row (index 7)
                if(i == 7){
                    cell.setBackgroundResource(R.drawable.total_dabba_bg);
                    cell.setTextColor(0xFF0D47A1);           // Dark Blue
                    cell.setTypeface(null, android.graphics.Typeface.BOLD);
                }

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

    private void showHolidayDialog(){

        Calendar cal = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {

                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);

                    String dateKey = keyFormat.format(selected.getTime());

                    EditText input = new EditText(getContext());
                    input.setHint("Enter Holiday Name");

                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Holiday Name")
                            .setView(input)
                            .setPositiveButton("Save", (d, w) -> {

                                String name = input.getText().toString();

                                SharedPreferences pref =
                                        getActivity().getSharedPreferences("holidays", 0);

                                pref.edit()
                                        .putString(dateKey, name)
                                        .apply();

                                updateMonth(); // refresh UI

                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }
}
