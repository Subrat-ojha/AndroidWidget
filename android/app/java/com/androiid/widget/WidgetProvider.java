package com.androiid.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * Home-screen widget. Android widgets cannot run the WebGL loop, so the
 * provider wakes up on a short alarm (see WidgetTicker) and swaps in the next
 * still frame. That is also where music/call detection happens, which means
 * the widget reacts even when the full app is closed.
 */
public class WidgetProvider extends AppWidgetProvider {

    static final String PREFS = "widget_prefs";
    static final String KEY = "scene";
    static final String KEY_STATE = "audio_state";
    static final String KEY_RESUME = "resume_scene";
    static final int MUSIC_I = 5;
    static final int CALL_I = 6;
    static final int[] SCENES = {
        R.drawable.scene0, R.drawable.scene1, R.drawable.scene2,
        R.drawable.scene3, R.drawable.scene4, R.drawable.scene5, R.drawable.scene6
    };

    @Override
    public void onUpdate(Context c, AppWidgetManager manager, int[] ids) {
        refresh(c, false, manager, ids);
        WidgetTicker.schedule(c);
    }

    @Override
    public void onEnabled(Context c) {
        refresh(c, false);
        WidgetTicker.schedule(c);
    }

    @Override
    public void onDisabled(Context c) {
        WidgetTicker.cancel(c);
    }

    /** "call" wins over "music"; anything else is idle. No permissions needed. */
    static String audioState(Context c) {
        try {
            AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return "idle";
            int mode = am.getMode();
            if (mode == AudioManager.MODE_IN_CALL
                    || mode == AudioManager.MODE_IN_COMMUNICATION) {
                return "call";
            }
            if (am.isMusicActive()) return "music";
        } catch (Throwable ignored) {
            // Some OEMs throw from AudioManager while the audio server restarts.
        }
        return "idle";
    }

    /**
     * Picks the scene. While music or a call is active the matching scene is
     * pinned until the state ends. When idle, the scene advances on each tick.
     */
    private static synchronized void updateScene(Context c, boolean autoAdvance) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, 0);
        int count = SCENES.length;
        int idx = ((sp.getInt(KEY, 0) % count) + count) % count;
        String prev = sp.getString(KEY_STATE, "idle");
        String state = audioState(c);

        if (!state.equals(prev)) {
            if ("idle".equals(prev)) {
                sp.edit().putInt(KEY_RESUME, idx).apply();
            }
            if ("call".equals(state)) {
                idx = CALL_I;
            } else if ("music".equals(state)) {
                idx = MUSIC_I;
            } else {
                idx = ((sp.getInt(KEY_RESUME, idx) % count) + count) % count;
            }
        } else if ("idle".equals(state) && autoAdvance) {
            idx = (idx + 1) % count;
        }

        sp.edit().putInt(KEY, idx).putString(KEY_STATE, state).apply();
    }

    static void refresh(Context c, boolean autoAdvance) {
        AppWidgetManager manager = AppWidgetManager.getInstance(c);
        ComponentName cn = new ComponentName(c, WidgetProvider.class);
        refresh(c, autoAdvance, manager, manager.getAppWidgetIds(cn));
    }

    private static void refresh(Context c, boolean autoAdvance,
                                AppWidgetManager manager, int[] ids) {
        if (ids == null || ids.length == 0) return;

        updateScene(c, autoAdvance);

        SharedPreferences sp = c.getSharedPreferences(PREFS, 0);
        int idx = ((sp.getInt(KEY, 0) % SCENES.length) + SCENES.length) % SCENES.length;

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        for (int id : ids) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
            rv.setImageViewResource(R.id.widget_image, SCENES[idx]);

            Intent cycle = new Intent(c, CycleReceiver.class);
            rv.setOnClickPendingIntent(R.id.widget_image,
                    PendingIntent.getBroadcast(c, 1, cycle, flags));

            Intent open = new Intent(c, MainActivity.class);
            rv.setOnClickPendingIntent(R.id.widget_open,
                    PendingIntent.getActivity(c, 2, open, flags));

            manager.updateAppWidget(id, rv);
        }
    }

    static void advanceByTap(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, 0);
        String state = audioState(c);
        if ("idle".equals(state)) {
            int next = (sp.getInt(KEY, 0) + 1) % SCENES.length;
            sp.edit().putInt(KEY, next).apply();
        }
        refresh(c, false);
    }
}
