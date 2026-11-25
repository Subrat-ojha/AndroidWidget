package com.androiid.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CycleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        SharedPreferences sp = c.getSharedPreferences(WidgetProvider.PREFS, 0);
        int next = (sp.getInt(WidgetProvider.KEY, 0) + 1) % WidgetProvider.SCENES.length;
        sp.edit().putInt(WidgetProvider.KEY, next).apply();
        WidgetProvider.refresh(c);
    }
}
