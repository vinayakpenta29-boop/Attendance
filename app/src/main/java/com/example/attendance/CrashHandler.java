package com.example.attendance;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        String error = Log.getStackTraceString(throwable);

        Log.e("APP_CRASH", error);

        Toast.makeText(context,
                "App Crashed\n\n" + error,
                Toast.LENGTH_LONG).show();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(1);
    }
}
