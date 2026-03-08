package com.example.attendance;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DailyAttendanceFragment(), "Daily Attendance");
        adapter.addFragment(new ViewAttendanceFragment(), "View Attendance");
        adapter.addFragment(new SalaryFragment(), "Salary");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
