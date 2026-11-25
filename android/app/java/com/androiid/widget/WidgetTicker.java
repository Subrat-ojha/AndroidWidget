package com.androiid.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/**
 * Drives the home-screen widget. A widget cannot run an animation, so every
 * few seconds we wake the receiver, read the audio state and push the next
 * still scene. Music and call states stay pinned until the audio state ends.
 */
public class WidgetTicker extends android.content.BroadcastReceiver {

    /** Fast enough to feel alive, slow enough not to drain the battery. */
    static final long INTERVAL_MS = 10000L;
    private static final int REQUEST_CODE = 3;
    private static final String ACTION = "com.androiid.widget.TICK";

    @Override
    public void onReceive(Context c, Intent intent) {
        WidgetProvider.refresh(c, true);
        schedule(c);
    }

    static PendingIntent pending(Context c) {
        Intent i = new Intent(c, WidgetTicker.class).setAction(ACTION);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(c, REQUEST_CODE, i, flags);
    }

    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        AppWidgetManager manager = AppWidgetManager.getInstance(c);
        int[] ids = manager.getAppWidgetIds(new ComponentName(c, WidgetProvider.class));
        PendingIntent pi = pending(c);

        if (ids == null || ids.length == 0) {
            am.cancel(pi);
            return;
        }

        long at = SystemClock.elapsedRealtime() + INTERVAL_MS;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Allowed without the exact-alarm permission, and still fires
                // while the phone is in Doze (music/calls keep it ticking).
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, at, pi);
            } else {
                am.set(AlarmManager.ELAPSED_REALTIME, at, pi);
            }
        } catch (Throwable ignored) {
            // If an OEM blocks the alarm the widget still advances on taps.
        }
    }

    static void cancel(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pending(c));
    }
}
