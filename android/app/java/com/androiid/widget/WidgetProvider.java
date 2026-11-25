package com.androiid.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    static final String PREFS = "widget_prefs";
    static final String KEY = "scene";
    static final int[] SCENES = {
        R.drawable.scene0, R.drawable.scene1, R.drawable.scene2,
        R.drawable.scene3, R.drawable.scene4, R.drawable.scene5, R.drawable.scene6
    };

    @Override
    public void onUpdate(Context c, AppWidgetManager manager, int[] ids) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, 0);
        int idx = sp.getInt(KEY, 0) % SCENES.length;

        for (int id : ids) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
            rv.setImageViewResource(R.id.widget_image, SCENES[idx]);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            Intent cycle = new Intent(c, CycleReceiver.class);
            rv.setOnClickPendingIntent(R.id.widget_image,
                    PendingIntent.getBroadcast(c, 1, cycle, flags));

            Intent open = new Intent(c, MainActivity.class);
            rv.setOnClickPendingIntent(R.id.widget_open,
                    PendingIntent.getActivity(c, 2, open, flags));

            manager.updateAppWidget(id, rv);
        }
    }

    static void refresh(Context c) {
        AppWidgetManager manager = AppWidgetManager.getInstance(c);
        ComponentName cn = new ComponentName(c, WidgetProvider.class);
        new WidgetProvider().onUpdate(c, manager, manager.getAppWidgetIds(cn));
    }
}
