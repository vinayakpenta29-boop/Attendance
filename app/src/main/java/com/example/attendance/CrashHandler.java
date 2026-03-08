package com.example.attendance;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        String error = Log.getStackTraceString(throwable);

        Log.e("APP_CRASH", error);

        new Thread(() -> {
            Looper.prepare();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("App Crashed");
            builder.setMessage(error);
            builder.setCancelable(false);
            builder.setPositiveButton("Close", (d, w) -> System.exit(1));
            builder.show();

            Looper.loop();
        }).start();
    }
}
