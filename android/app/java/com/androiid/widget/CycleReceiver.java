package com.androiid.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Tap on the widget picture: show the next scene and restart the timer. */
public class CycleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        WidgetProvider.advanceByTap(c);
        WidgetTicker.schedule(c);
    }
}
