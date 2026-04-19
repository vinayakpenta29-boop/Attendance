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
import java.util.Collections;

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
        else if(item.getItemId() == R.id.menu_view_sales){
            showSalesViewDialog(); // ✅ NEW
            return true;
        }
        else if(item.getItemId() == R.id.menu_extra_dabba){
            showExtraDabbaDialog();
            return true;
        }
        else if(item.getItemId() == R.id.menu_amavasya){
            showAmavasyaDialog();
            return true;
        }
        else if(item.getItemId() == R.id.menu_view_leaves){
            showLeavesDialog(); // ✅ NEW METHOD
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
        SharedPreferences holidayPref =
            getActivity().getSharedPreferences("holidays", 0);
        SharedPreferences amavasyaPref =
            getActivity().getSharedPreferences("amavasya", 0);

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
                cell.setBackgroundResource(R.drawable.calendar_cell_bg);
                

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

                TextView extraDabbaText = new TextView(getContext());
                extraDabbaText.setTextSize(14);
                extraDabbaText.setTextColor(0xFF8E24AA); // Purple
                extraDabbaText.setGravity(Gravity.CENTER);
                extraDabbaText.setTypeface(null, android.graphics.Typeface.BOLD);

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

                    // ✅ Apply Sunday background ONLY when date exists
                    if(i == 0){
                        cell.setBackgroundResource(R.drawable.sunday_cell_bg);
                    }

                    dayNumber.setText(String.valueOf(dayCounter));

                    Calendar tempCal = Calendar.getInstance();
                    tempCal.set(year, month, dayCounter);

                    String dateKey = keyFormat.format(tempCal.getTime());
                    String displayDate = displayFormat.format(tempCal.getTime());

                    String amavasyaName = amavasyaPref.getString(dateKey, null);
                    
                    String status = pref.getString(dateKey,"");
                    
                    String dabba = pref.getString(dateKey + "_dabba","");

                    String extraDabba = pref.getString(dateKey + "_extra_dabba","");

                    String holidayName = holidayPref.getString(dateKey, null);
                    
                    // ✅ Make holiday date number RED
                    if(holidayName != null){
                        dayNumber.setTextColor(0xFFC62828); // same as Sunday
                        dayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
                        dayNumber.setBackgroundResource(R.drawable.holiday_date_bg);
                    }
                    else if(amavasyaName != null){
                        dayNumber.setTextColor(0xFFF2BD18); // same as Sunday
                        dayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
                        dayNumber.setBackgroundResource(R.drawable.total_commission_bg);
                    }

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
                    extraDabbaText.setText(extraDabba);

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
                    }

                    // ✅ ALWAYS allow holiday click (even if attendance exists)
                    if(holidayName != null){
                        cell.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(getContext())
                                    .setTitle("Holiday")
                                    .setMessage(holidayName)
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    }

                    if(amavasyaName != null){
                        cell.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(getContext())
                                    .setTitle("Amavasya")
                                    .setMessage(amavasyaName)
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    }

                    // ✅ LONG PRESS → Edit/Delete Holiday
                    if(holidayName != null){
                        cell.setOnLongClickListener(v -> {

                            String[] options = {"Edit Holiday", "Delete Holiday"};

                            new android.app.AlertDialog.Builder(getContext())
                                    .setTitle("Manage Holiday")
                                    .setItems(options, (dialog, which) -> {

                                        if(which == 0){
                                            // ✏️ EDIT
                                            EditText input = new EditText(getContext());
                                            input.setText(holidayName);

                                            new android.app.AlertDialog.Builder(getContext())
                                                    .setTitle("Edit Holiday Name")
                                                    .setView(input)
                                                    .setPositiveButton("Update", (d, w) -> {

                                                        String newName = input.getText().toString();

                                                        holidayPref.edit()
                                                                .putString(dateKey, newName)
                                                                .apply();

                                                        updateMonth(); // refresh UI
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .show();

                                        } else if(which == 1){
                            
                                            holidayPref.edit()
                                                    .remove(dateKey)
                                                    .apply();

                                            updateMonth(); // refresh UI
                                        }

                                    })
                                    .show();

                            return true;
                        });
                    }

                    if(amavasyaName != null){
                        cell.setOnLongClickListener(v -> {

                            String[] options = {"Edit Amavasya", "Delete Amavasya"};

                            new android.app.AlertDialog.Builder(getContext())
                                    .setTitle("Manage Amavasya")
                                    .setItems(options, (dialog, which) -> {

                                        if(which == 0){
                                            // ✏️ EDIT
                                            EditText input = new EditText(getContext());
                                            input.setText(amavasyaName);

                                            new android.app.AlertDialog.Builder(getContext())
                                                    .setTitle("Edit Amavasya Name")
                                                    .setView(input)
                                                    .setPositiveButton("Update", (d, w) -> {

                                                        String newName = input.getText().toString();

                                                        amavasyaPref.edit()
                                                                .putString(dateKey, newName)
                                                                .apply();

                                                        updateMonth(); // refresh UI
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .show();

                                        } else if(which == 1){
                            
                                            amavasyaPref.edit()
                                                    .remove(dateKey)
                                                    .apply();

                                            updateMonth(); // refresh UI
                                        }

                                    })
                                    .show();

                            return true;
                        });
                    }

                    dayCounter++;
                }

                cell.addView(dayNumber);
                cell.addView(statusText);
                cell.addView(divider);
                cell.addView(dabbaText);
                cell.addView(extraDabbaText);

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

    private void showSalesViewDialog(){

    SharedPreferences pref =
            getActivity().getSharedPreferences("sales_data", 0);

    Calendar cal = (Calendar) currentCalendar.clone();

    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);

    cal.set(Calendar.DAY_OF_MONTH, 1);

    int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

    // ✅ ROOT LAYOUT
    LinearLayout root = new LinearLayout(getContext());
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(20,20,20,20);

    ScrollView scrollView = new ScrollView(getContext());
    scrollView.addView(root);

    // ✅ TABLE
    TableLayout table = new TableLayout(getContext());
    table.setStretchAllColumns(true);

    /* ===== HEADER ROW ===== */
    TableRow header = new TableRow(getContext());
    header.setBackgroundColor(0xFF3F51B5);

    String[] titles = {"Date", "Amount"};

    for(String t : titles){
        TextView tv = new TextView(getContext());
        tv.setText(t);
        tv.setPadding(20,20,20,20);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);

        TableRow.LayoutParams headerParams =
                new TableRow.LayoutParams(0,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f);
        headerParams.setMargins(6,6,6,6);
        tv.setLayoutParams(headerParams);

        header.addView(tv);
    }

    table.addView(header);

    int totalSales = 0;
    boolean hasData = false;
    double commission = 0;
    boolean isEligible = false;
    double schemeAmount = 0;

    boolean isSchemeForced = false; // 👈 ADD THIS

    TableRow schemeRow = new TableRow(getContext());
    TextView sc1 = new TextView(getContext());
    TextView sc2 = new TextView(getContext());

    TableRow finalRow = new TableRow(getContext());
    TextView f1 = new TextView(getContext());
    TextView f2 = new TextView(getContext());

    /* ===== DATA ROWS ===== */
    for(int day = 1; day <= daysInMonth; day++){

        Calendar temp = Calendar.getInstance();
        temp.set(year, month, day);

        String key = keyFormat.format(temp.getTime());

        int amount = pref.getInt(key, 0);

        if(amount > 0){

            hasData = true;
            totalSales += amount;

            TableRow row = new TableRow(getContext());

            if(day % 2 == 0)
                row.setBackgroundColor(0xFFF7F9FC);
            else
                row.setBackgroundColor(0xFFFFFFFF);

            TextView dateCell = new TextView(getContext());
            TextView amountCell = new TextView(getContext());

            dateCell.setText(displayFormat.format(temp.getTime()));
            amountCell.setText(formatRupees(amount));

            TextView[] cells = {dateCell, amountCell};

            for(TextView c : cells){
                c.setPadding(18,18,18,18);
                c.setGravity(Gravity.CENTER);
                c.setTextSize(14);
                c.setBackgroundResource(R.drawable.history_cell_bg);

                TableRow.LayoutParams cellParams =
                        new TableRow.LayoutParams(0,
                                TableRow.LayoutParams.WRAP_CONTENT,1f);
                cellParams.setMargins(6,6,6,6);
                c.setLayoutParams(cellParams);
            }

            row.addView(dateCell);
            row.addView(amountCell);

            table.addView(row);

            // ✅ LONG PRESS FOR EDIT / DELETE SALES
            row.setOnLongClickListener(v -> {

                    String[] options = {"Edit Sales", "Delete Sales"};

                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Manage Sales")
                            .setItems(options, (dialog, which) -> {

                                if(which == 0){
                                  // ✏️ EDIT SALES

                                    EditText input = new EditText(getContext());
                                    input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                                    input.setText(String.valueOf(amount));

                                    new android.app.AlertDialog.Builder(getContext())
                                            .setTitle("Edit Sales Amount")
                                            .setView(input)
                                            .setPositiveButton("Update", (d, w) -> {

                                                int newAmount = 0;
                                                try{
                                                    newAmount = Integer.parseInt(input.getText().toString());
                                                    }catch(Exception e){
                                                    newAmount = 0;
                                                }

                                                SharedPreferences.Editor editor = pref.edit();

                                                if(newAmount > 0){
                                                    editor.putInt(key, newAmount);
                                                } else {
                                                    editor.remove(key);
                                                }

                                                editor.apply();

                                                showSalesViewDialog(); // 🔥 refresh dialog
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();

                                 } else if(which == 1){
                                    // 🗑 DELETE SALES

                                    new android.app.AlertDialog.Builder(getContext())
                                            .setTitle("Delete Sales")
                                            .setMessage("Are you sure?")
                                            .setPositiveButton("Delete", (d, w) -> {

                                                pref.edit().remove(key).apply();

                                                showSalesViewDialog(); // 🔥 refresh dialog
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                }

                            })
                            .show();
 
                    return true;
                });
        }
    }

    View divider3 = new View(getContext());
    divider3.setLayoutParams(new TableRow.LayoutParams(
    TableRow.LayoutParams.MATCH_PARENT, 2));
    divider3.setBackgroundColor(0xFFDDDDDD);
    divider3.setPadding(20,40,20,40);
        
        table.addView(divider3);

    // ✅ GET ATTENDANCE DATA
    SharedPreferences attendancePref =
            getActivity().getSharedPreferences("attendance", 0);

    SharedPreferences holidayPref =
            getActivity().getSharedPreferences("holidays", 0);

    double leaveCount = 0;
    boolean tookLeaveOnHoliday = false;

    for(int day = 1; day <= daysInMonth; day++){

        Calendar temp = Calendar.getInstance();
        temp.set(year, month, day);

        String key = keyFormat.format(temp.getTime());

        String status = attendancePref.getString(key, "");
        String holidayName = holidayPref.getString(key, null);

        // Count leaves
        if(status.equals("Absent")){
            leaveCount++;
        }
        else if(status.equals("Half Day")){
            leaveCount += 0.5;
        }

        if(holidayName != null &&
                (status.equals("Absent") || status.equals("Half Day"))){
            tookLeaveOnHoliday = true;
        }

        // ✅ Sunday leave ONLY if Absent or Half Day
        
        int dayOfWeek = temp.get(Calendar.DAY_OF_WEEK);

        // ✅ Sunday OR Saturday leave
        if((dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) &&
                (status.equals("Absent") || status.equals("Half Day"))){
            tookLeaveOnHoliday = true;
        }
    }

    /* ===== NO DATA ===== */
    if(!hasData){
        TextView tv = new TextView(getContext());
        tv.setText("No Sales Found for this month");
        tv.setPadding(20,20,20,20);
        root.addView(tv);
    }
    else{

        root.addView(table);

        /* ===== TOTAL ROW ===== */
        TableRow totalRow = new TableRow(getContext());

        TextView t1 = new TextView(getContext());
        TextView t2 = new TextView(getContext());

        t1.setText("Total Sales");
        t2.setText(formatRupees(totalSales));

        t1.setTypeface(null, android.graphics.Typeface.BOLD);
        t2.setTypeface(null, android.graphics.Typeface.BOLD);

        t1.setTextColor(0xFF1B5E20); // Dark Green
        t2.setTextColor(0xFF1B5E20);

        TextView[] totalCells = {t1, t2};

        for(TextView c : totalCells){
            c.setPadding(20,20,20,20);
            c.setGravity(Gravity.CENTER);
            c.setTextSize(16);
            c.setBackgroundResource(R.drawable.total_commission_bg);

            TableRow.LayoutParams totalParams =
                    new TableRow.LayoutParams(0,
                            TableRow.LayoutParams.WRAP_CONTENT,1f);
            totalParams.setMargins(6,6,6,6);
            c.setLayoutParams(totalParams);
        }

        totalRow.addView(t1);
        totalRow.addView(t2);

        table.addView(totalRow);

        // ✅ COMMISSION CALCULATION
        double afterFivePercent = totalSales * 0.95;
        commission = afterFivePercent * 0.01;

        isEligible = (leaveCount <= 3) && !tookLeaveOnHoliday;

        
        /* ===== COMMISSION ROW ===== */
        TableRow commissionRow = new TableRow(getContext());

        TextView c1 = new TextView(getContext());
        TextView c2 = new TextView(getContext());

        c1.setText("Commission");
        c2.setText(formatRupees((int) commission));

        c1.setTypeface(null, android.graphics.Typeface.BOLD);
        c2.setTypeface(null, android.graphics.Typeface.BOLD);

        c1.setTextColor(0xFF6A1B9A); // Purple (premium look)
        c2.setTextColor(0xFF6A1B9A);

        TextView[] commissionCells = {c1, c2};

        for(TextView c : commissionCells){
            c.setPadding(20,20,20,20);
            c.setGravity(Gravity.CENTER);
            c.setTextSize(16);
            c.setBackgroundResource(R.drawable.total_commission_bg);

            TableRow.LayoutParams rowParams =
                    new TableRow.LayoutParams(0,
                            TableRow.LayoutParams.WRAP_CONTENT,1f);
            rowParams.setMargins(6,6,6,6);
            c.setLayoutParams(rowParams);
        }

        commissionRow.addView(c1);
        commissionRow.addView(c2);

        table.addView(commissionRow);

                // ✅ ADD DIVIDER HERE
        View divider1 = new View(getContext());
        divider1.setLayoutParams(new TableRow.LayoutParams(
        TableRow.LayoutParams.MATCH_PARENT, 2));
        divider1.setBackgroundColor(0xFFDDDDDD);
        divider1.setPadding(20,40,20,20);

        table.addView(divider1);

        /* ===== SCHEME STATUS ===== */
        TableRow statusRow = new TableRow(getContext());

        TextView statusText = new TextView(getContext());

        if(isEligible){
            statusText.setText("You are in Scheme");
            statusText.setTextColor(0xFF2E7D32); // Green
        } else {
            statusText.setText("You are Not in Scheme");
            statusText.setTextColor(0xFFC62828); // Red
        }

        statusText.setPadding(60,30,20,30);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTextSize(18);
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);

        statusRow.addView(statusText);

        table.addView(statusRow);

        // ✅ ADD DIVIDER HERE
        View divider2 = new View(getContext());
        divider2.setLayoutParams(new TableRow.LayoutParams(
        TableRow.LayoutParams.MATCH_PARENT, 2));
        divider2.setBackgroundColor(0xFFDDDDDD);
        divider2.setPadding(20,20,20,40);
        
        table.addView(divider2);

        // ===== SCHEME ROW (CREATE ONCE) =====
        sc1.setText("Scheme");

        sc1.setTypeface(null, android.graphics.Typeface.BOLD);
        sc2.setTypeface(null, android.graphics.Typeface.BOLD);

        sc1.setTextColor(0xFF0D47A1);
        sc2.setTextColor(0xFF0D47A1);

        TextView[] schemeCells = {sc1, sc2};

        for(TextView c : schemeCells){
            c.setPadding(20,20,20,20);
            c.setGravity(Gravity.CENTER);
            c.setTextSize(16);
            c.setBackgroundResource(R.drawable.total_dabba_bg);

            TableRow.LayoutParams rp =
                    new TableRow.LayoutParams(0,
                            TableRow.LayoutParams.WRAP_CONTENT,1f);
            rp.setMargins(6,6,6,6);
            c.setLayoutParams(rp);
        }

        schemeRow.addView(sc1);
        schemeRow.addView(sc2);


        // ===== FINAL ROW (CREATE ONCE) =====
        

        f1.setText("Final Amount");

        f1.setTypeface(null, android.graphics.Typeface.BOLD);
        f2.setTypeface(null, android.graphics.Typeface.BOLD);

        f1.setTextColor(0xFF004D40);
        f2.setTextColor(0xFF004D40);

        TextView[] finalCells = {f1, f2};

        for(TextView c : finalCells){
            c.setPadding(20,20,20,20);
            c.setGravity(Gravity.CENTER);
            c.setTextSize(16);
            c.setBackgroundResource(R.drawable.total_leaves_bg);

            TableRow.LayoutParams fp =
                    new TableRow.LayoutParams(0,
                            TableRow.LayoutParams.WRAP_CONTENT,1f);
            fp.setMargins(6,6,6,6);
            c.setLayoutParams(fp);
        }

        finalRow.addView(f1);
        finalRow.addView(f2);


        // ===== SET VALUES (ONLY ONCE) =====
        if(isEligible){
            schemeAmount = commission / 2;
            sc2.setText(formatRupees((int) schemeAmount));
        } else {
            schemeAmount = 0;
            sc2.setText("₹ 0");
        }

        double finalAmount = commission + schemeAmount;
        f2.setText(formatRupees((int) finalAmount));


        // ===== ADD ROWS (CORRECT ORDER) =====
        if(isEligible){
            table.addView(schemeRow);
        }
        table.addView(finalRow);

        final double finalCommission = commission;

        // ===============================
// ✅ SCHEME ENABLE TOGGLE (ONLY WHEN NOT ELIGIBLE)
// ===============================
if(!isEligible){

    TableRow toggleRow = new TableRow(getContext());

    LinearLayout toggleLayout = new LinearLayout(getContext());
    toggleLayout.setOrientation(LinearLayout.HORIZONTAL);
    toggleLayout.setGravity(Gravity.CENTER);
    toggleLayout.setPadding(20,30,20,30);

    TextView toggleText = new TextView(getContext());
    toggleText.setText("Enable Scheme");
    toggleText.setTextSize(16);
    toggleText.setTypeface(null, android.graphics.Typeface.BOLD);

    Switch toggle = new Switch(getContext());

    toggleLayout.addView(toggleText);
    toggleLayout.addView(toggle);

    toggleRow.addView(toggleLayout);
    table.addView(toggleRow);

    // 👉 When toggle changes
    toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {

        double forcedScheme = isChecked ? finalCommission / 2 : 0;

        // ✅ Update Scheme Row
        if(isChecked){
            if(table.indexOfChild(schemeRow) == -1){
                table.addView(schemeRow, table.indexOfChild(finalRow));
            }
            sc2.setText(formatRupees((int) forcedScheme));
        } else {
            table.removeView(schemeRow);
        }

        // ✅ Update Final Amount ONLY (NO NEW ROW)
        double newFinal = finalCommission + forcedScheme;
        f2.setText(formatRupees((int) newFinal));
    });
    }

    /* ===== DIALOG ===== */
    TextView title = new TextView(getContext());
    title.setText("Monthly Sales Report");
    title.setTextColor(0xFF990F4B); // your red shade
    title.setTextSize(16);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    title.setPadding(30,30,30,10);
    title.setGravity(Gravity.CENTER);
    
    new android.app.AlertDialog.Builder(getContext())
            .setCustomTitle(title)
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show();
}
}

    private String formatRupees(int amount){
    java.text.NumberFormat formatter =
            java.text.NumberFormat.getInstance(new Locale("en", "IN"));
    return "₹ " + formatter.format(amount);
    }

    private void showExtraDabbaDialog(){

    Calendar cal = Calendar.getInstance();

    DatePickerDialog picker = new DatePickerDialog(getContext(),
            (view, year, month, day) -> {

                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);

                String dateKey = keyFormat.format(selected.getTime());

                // ✅ Spinner
                Spinner spinner = new Spinner(getContext());

                String[] options = {"Absent (A)", "Dabba (D)", "Late (L)", "Ghari (G)"};

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        options
                );

                spinner.setAdapter(adapter);

                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Select Extra Dabba")
                        .setView(spinner)
                        .setPositiveButton("Save", (d, w) -> {

                            String selectedItem = spinner.getSelectedItem().toString();
                            String value = "";

                            if(selectedItem.contains("A")) value = "A";
                            else if(selectedItem.contains("D")) value = "D";
                            else if(selectedItem.contains("L")) value = "L";
                            else if(selectedItem.contains("G")) value = "G";

                            SharedPreferences pref =
                                    getActivity().getSharedPreferences("attendance", 0);

                            // ✅ SAVE EXTRA DABBA
                            pref.edit()
                                    .putString(dateKey + "_extra_dabba", value)
                                    .apply();

                            updateMonth(); // 🔥 refresh UI

                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));

    picker.show();
}

    private void showAmavasyaDialog(){

Calendar cal = Calendar.getInstance();  

DatePickerDialog picker = new DatePickerDialog(getContext(),  
        (view, year, month, day) -> {  

            Calendar selected = Calendar.getInstance();  
            selected.set(year, month, day);  

            String dateKey = keyFormat.format(selected.getTime());  

            EditText input = new EditText(getContext());  
            input.setHint("Enter Amavasya Name");  

            new android.app.AlertDialog.Builder(getContext())  
                    .setTitle("Amavasya Name")  
                    .setView(input)  
                    .setPositiveButton("Save", (d, w) -> {  

                        String name = input.getText().toString();  

                        SharedPreferences pref =  
                                getActivity().getSharedPreferences("amavasya", 0);  

                        pref.edit()  
                                .putString(dateKey, name)  
                                .apply();  

                        updateMonth(); // 🔥 refresh UI  
                    })  
                    .setNegativeButton("Cancel", null)  
                    .show();  

        },  
        cal.get(Calendar.YEAR),  
        cal.get(Calendar.MONTH),  
        cal.get(Calendar.DAY_OF_MONTH));  

picker.show();

}

    private void showLeavesDialog(){

    SharedPreferences pref =
            getActivity().getSharedPreferences("attendance", 0);

    LinearLayout root = new LinearLayout(getContext());
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(30,30,30,30);

    // ===== GET AVAILABLE YEARS FROM DATA =====
    ArrayList<String> yearList = new ArrayList<>();

    for(String key : pref.getAll().keySet()){

        try{
            // key format = yyyy-MM-dd
            String year = key.substring(0,4);

            if(!yearList.contains(year)){
                yearList.add(year);
            }

        }catch(Exception e){
            // ignore invalid keys
        }
    }

    // 👉 If no data, show current year
    if(yearList.isEmpty()){
        Calendar cal = Calendar.getInstance();
        yearList.add(String.valueOf(cal.get(Calendar.YEAR)));
    }

    // Optional: sort years
    Collections.sort(yearList, Collections.reverseOrder());

    // Convert to array
    String[] years = yearList.toArray(new String[0]);

    LinearLayout spinnerContainer = new LinearLayout(getContext());
    spinnerContainer.setOrientation(LinearLayout.VERTICAL);
    spinnerContainer.setPadding(20,20,20,20);
    spinnerContainer.setBackgroundResource(R.drawable.history_cell_bg);

    LinearLayout.LayoutParams containerParams =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
    containerParams.setMargins(0,0,0,20);
    spinnerContainer.setLayoutParams(containerParams);

    // ===== SPINNER =====
    Spinner yearSpinner = new Spinner(getContext());

    ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_dropdown_item,
            years
    );

    yearSpinner.setAdapter(yearAdapter);
    yearSpinner.setPadding(20,10,20,10);

    spinnerContainer.addView(yearSpinner);

    root.addView(spinnerContainer);

    // ===== RESULT TABLE =====
    TableLayout table = new TableLayout(getContext());
    table.setStretchAllColumns(true);

    root.addView(table);

    // ===== FUNCTION TO LOAD DATA =====
    Runnable loadData = () -> {

        table.removeAllViews();

        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());

        String[] months = {
                "January","February","March","April","May","June",
                "July","August","September","October","November","December"
        };

        // HEADER
        TableRow header = new TableRow(getContext());

        String[] titles = {"Month", "Total Leaves"};

        for(String t : titles){
            TextView tv = new TextView(getContext());
            tv.setText(t);
            tv.setPadding(20,20,20,20);
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setBackgroundColor(0xFF3F51B5);
            tv.setTextColor(0xFFFFFFFF);

            header.addView(tv);
        }

        table.addView(header);

        double yearlyTotal = 0;

        // ===== MONTH LOOP =====
        for(int m = 0; m < 12; m++){

            Calendar cal = Calendar.getInstance();
            cal.set(selectedYear, m, 1);

            int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            double totalLeaves = 0;

            for(int d = 1; d <= days; d++){

                Calendar temp = Calendar.getInstance();
                temp.set(selectedYear, m, d);

                String key = keyFormat.format(temp.getTime());

                String status = pref.getString(key, "");

                if(status.equals("Absent")){
                    totalLeaves++;
                }
                else if(status.equals("Half Day")){
                    totalLeaves += 0.5;
                }
                
            }

            yearlyTotal += totalLeaves;

            // ROW
            TableRow row = new TableRow(getContext());

            TextView monthCell = new TextView(getContext());
            TextView valueCell = new TextView(getContext());

            monthCell.setText(months[m]);
            valueCell.setText(String.valueOf(totalLeaves));

            TextView[] cells = {monthCell, valueCell};

            for(TextView c : cells){
                c.setPadding(18,18,18,18);
                c.setGravity(Gravity.CENTER);
                c.setTextSize(14);
                c.setBackgroundResource(R.drawable.history_cell_bg);

                TableRow.LayoutParams params =
                        new TableRow.LayoutParams(0,
                                TableRow.LayoutParams.WRAP_CONTENT,1f);
                params.setMargins(6,6,6,6);
                c.setLayoutParams(params);
            }

            // Highlight if high leaves
            if(totalLeaves > 3){
                valueCell.setTextColor(0xFFC62828); // red
            }
            else if(totalLeaves <= 3){
                valueCell.setTextColor(0xFF5FC40C); // green
            }

            row.addView(monthCell);
            row.addView(valueCell);

            table.addView(row);
        };
    

    TableRow totalRow = new TableRow(getContext());

        TextView t1 = new TextView(getContext());
        TextView t2 = new TextView(getContext());

        t1.setText("Total Yearly Leaves");
        t2.setText(String.valueOf(yearlyTotal));

        t1.setTypeface(null, android.graphics.Typeface.BOLD);
        t2.setTypeface(null, android.graphics.Typeface.BOLD);

        t1.setTextColor(0xFFB71C1C); // Dark Red
        t2.setTextColor(0xFFB71C1C);

        TextView[] totalCells = {t1, t2};

        for(TextView c : totalCells){
            c.setPadding(20,20,20,20);
            c.setGravity(Gravity.CENTER);
            c.setTextSize(16);
            c.setBackgroundResource(R.drawable.total_commission_bg);

            TableRow.LayoutParams params =
                    new TableRow.LayoutParams(0,
                            TableRow.LayoutParams.WRAP_CONTENT,1f);
            params.setMargins(6,6,6,6);
            c.setLayoutParams(params);
        }

        totalRow.addView(t1);
        totalRow.addView(t2);

        table.addView(totalRow);
    };

    // INITIAL LOAD
    loadData.run();

    // ON YEAR CHANGE
    yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            loadData.run();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    });

    // ===== SHOW DIALOG =====
    TextView year = new TextView(getContext());
    year.setText("Monthly Sales Report");
    year.setTextColor(0xFF990F4B); // your red shade
    year.setTextSize(16);
    year.setTypeface(null, android.graphics.Typeface.BOLD);
    year.setPadding(30,30,30,10);
    year.setGravity(Gravity.CENTER);
        
    new android.app.AlertDialog.Builder(getContext())
            .setCustomTitle(year)
            .setView(root)
            .setPositiveButton("OK", null)
            .show();
}
}
