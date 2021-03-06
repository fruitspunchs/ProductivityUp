/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.Serializable;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.libs.Utility;
import io.github.fruitspunchs.productivityup.ui.MainActivity;
import io.github.fruitspunchs.productivityup.ui.PomodoroTimerCard;
import io.github.fruitspunchs.productivityup.ui.UltradianRhythmTimerCard;
import io.github.fruitspunchs.productivityup.widget.TimerAppWidgetProvider;

/**
 * Runs timer and updates widgets.
 */

public class TimerService extends Service {
    public final static String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public final static String ACTION_ON_UPDATE = "ACTION_ON_UPDATE";
    public final static String ACTION_START_PAUSE_TIMER = "ACTION_START_PAUSE_TIMER";
    public final static String ACTION_WORK_REST_TIMER = "ACTION_WORK_REST_TIMER";
    public final static String ACTION_START_ULTRADIAN_TIMER = "ACTION_START_ULTRADIAN_TIMER";
    public static final String ACTION_REQUEST_POMODORO_STATE = "ACTION_REQUEST_POMODORO_STATE";
    public static final String ACTION_REQUEST_ULTRADIAN_STATE = "ACTION_REQUEST_ULTRADIAN_STATE";
    public final static String APP_WIDGET_IDS_KEY = "APP_WIDGET_IDS_KEY";

    public static final String POMODORO_EVENT = "POMODORO_EVENT";
    public static final String POMODORO_EVENT_KEY = "POMODORO_EVENT_KEY";
    public static final String POMODORO_EVENT_TIME_LEFT = "POMODORO_EVENT_TIME_LEFT";
    public static final String POMODORO_EVENT_TIME_LEFT_KEY = "POMODORO_EVENT_TIME_LEFT_KEY";
    public static final String POMODORO_EVENT_BUTTON_STATE = "POMODORO_EVENT_BUTTON_STATE";
    public static final String POMODORO_EVENT_BUTTON_STATE_KEY = "POMODORO_EVENT_BUTTON_STATE_KEY";

    public static final String ULTRADIAN_EVENT = "ULTRADIAN_EVENT";
    public static final String ULTRADIAN_EVENT_KEY = "ULTRADIAN_EVENT_KEY";
    public static final String ULTRADIAN_EVENT_TIME_LEFT = "ULTRADIAN_EVENT_TIME_LEFT";
    public static final String ULTRADIAN_EVENT_TIME_LEFT_KEY = "ULTRADIAN_EVENT_TIME_LEFT_KEY";
    public static final String ULTRADIAN_EVENT_BUTTON_STATE = "ULTRADIAN_EVENT_BUTTON_STATE";
    public static final String ULTRADIAN_EVENT_BUTTON_STATE_KEY = "ULTRADIAN_EVENT_BUTTON_STATE_KEY";

    private final static String ULTRADIAN_RHYTHM_START_TIME_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_START_TIME_KEY;
    private final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = UltradianRhythmTimerCard.ULTRADIAN_RHYTHM_WORK_REST_KEY;
    private final static int WORK = UltradianRhythmTimerCard.WORK;
    private final static int REST = UltradianRhythmTimerCard.REST;
    private final static int WORK_DURATION = UltradianRhythmTimerCard.WORK_DURATION;
    private final static int REST_DURATION = UltradianRhythmTimerCard.REST_DURATION;

    private final static String POMODORO_TIMER_START_TIME_KEY = PomodoroTimerCard.POMODORO_TIMER_START_TIME_KEY;
    private final static String POMODORO_TIMER_START_PAUSE_KEY = PomodoroTimerCard.POMODORO_TIMER_START_PAUSE_KEY;
    private final static String POMODORO_TIMER_TIME_LEFT_KEY = PomodoroTimerCard.POMODORO_TIMER_TIME_LEFT_KEY;
    private final static int TIMER_MAX_DURATION = PomodoroTimerCard.TIMER_MAX_DURATION;
    private final static int START = PomodoroTimerCard.START;
    private final static int PAUSE = PomodoroTimerCard.PAUSE;
    private final static int STOP = PomodoroTimerCard.STOP;

    private static CountDownTimer sUltradianRhythmCountDownTimer;
    private static CountDownTimer sPomodoroCountDownTimer;

    private final String TAG = getClass().getSimpleName();
    boolean mHasConfigurationChanged = false;
    private int mRhythmState;
    private int[] mAppWidgetIds;
    private int mStartPauseState;
    private long mPomodoroTimeLeft;
    private SharedPreferences mSharedPreferences;
    private MediaPlayer mMediaPlayer;
    private ServiceHandler mServiceHandler;
    private long mMinutesLeft = 0;
    private AppWidgetManager mUltradianCountdownTimerAppWidgetManager;
    private RemoteViews mUltradianCountdownTimerRemoteViews;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("TimerThread");
        thread.start();

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAppWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(getPackageName(), TimerAppWidgetProvider.class.getName()));

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, alert);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                sendMessageToServiceHandler(intent);
            }
        } else if (mAppWidgetIds.length > 0) {
            sendMessageToServiceHandler(new Intent(ACTION_START_SERVICE));
        }

        if (mAppWidgetIds.length > 0) {
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setWidgetIntents(RemoteViews remoteViews) {
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0);

        Intent startPauseTimerIntent = new Intent(this, TimerService.class);
        startPauseTimerIntent.setAction(TimerService.ACTION_START_PAUSE_TIMER);
        PendingIntent startPauseTimerPendingIntent = PendingIntent.getService(this, 1, startPauseTimerIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.layout_container, startActivityPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.start_pause_button, startPauseTimerPendingIntent);
    }

    public void startUltradianRhythmTimer() {
        Log.d(TAG, "startUltradianRhythmTimer");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        setWidgetIntents(remoteViews);

        long startTime;

        if (mSharedPreferences.contains(ULTRADIAN_RHYTHM_START_TIME_KEY)) {
            startTime = mSharedPreferences.getLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds());
            mRhythmState = mSharedPreferences.getInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, WORK);
        } else {
            startTime = Utility.getCurrentTimeInSeconds();
            mRhythmState = WORK;
            mSharedPreferences.edit()
                    .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                    .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                    .apply();
        }

        long timeElapsed = Utility.getCurrentTimeInSeconds() - startTime;

        if (mRhythmState == REST) {
            if (timeElapsed >= REST_DURATION) {
                timeElapsed -= REST_DURATION;
                mRhythmState = WORK;
            }
        }

        if (mRhythmState == WORK) {
            while (timeElapsed >= WORK_DURATION) {
                timeElapsed -= WORK_DURATION;
                mRhythmState = REST;

                if (timeElapsed >= REST_DURATION) {
                    timeElapsed -= REST_DURATION;
                    mRhythmState = WORK;
                } else {
                    break;
                }
            }
        }

        long timeLeft = 0;
        if (mRhythmState == WORK) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            remoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_work_icon));
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            remoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_rest_icon));
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, REST);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        sUltradianRhythmCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMinutesLeft = millisUntilFinished / (1000 * 60);
                broadcastUltradianMessage(ULTRADIAN_EVENT_TIME_LEFT, ULTRADIAN_EVENT_TIME_LEFT_KEY, mMinutesLeft);

                mUltradianCountdownTimerAppWidgetManager = AppWidgetManager.getInstance(TimerService.this);
                mUltradianCountdownTimerRemoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

                if (mHasConfigurationChanged) {
                    updateWidgetViews();
                    mHasConfigurationChanged = false;
                } else {
                    mUltradianCountdownTimerRemoteViews.setTextViewText(R.id.ultradian_rhythm_status_text_view, Utility.formatUltradianTimeString(mMinutesLeft));
                    mUltradianCountdownTimerRemoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

                    for (int appWidgetId : mAppWidgetIds) {
                        mUltradianCountdownTimerAppWidgetManager.updateAppWidget(appWidgetId, mUltradianCountdownTimerRemoteViews);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (mRhythmState == WORK) {
                    mRhythmState = REST;
                } else if (mRhythmState == REST) {
                    mRhythmState = WORK;
                }

                mSharedPreferences.edit()
                        .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                        .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                        .apply();

                Intent serviceIntent = new Intent(TimerService.this, TimerService.class);
                serviceIntent.setAction(ACTION_START_ULTRADIAN_TIMER);
                startService(serviceIntent);
            }
        }.start();

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    public void initializePomodoroTimer() {
        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        setWidgetIntents(remoteViews);

        Log.d(TAG, "Pomodoro timer initialized");

        long startTime;
        long currentTime = Utility.getCurrentTimeInSeconds();
        if (mSharedPreferences.contains(POMODORO_TIMER_START_TIME_KEY)) {
            Log.d(TAG, "Found preferences");
            startTime = mSharedPreferences.getLong(POMODORO_TIMER_START_TIME_KEY, currentTime);
            mStartPauseState = mSharedPreferences.getInt(POMODORO_TIMER_START_PAUSE_KEY, START);
            mPomodoroTimeLeft = mSharedPreferences.getLong(POMODORO_TIMER_TIME_LEFT_KEY, 0);
        } else {
            Log.d(TAG, "No preferences");
            startTime = currentTime;
            mStartPauseState = PAUSE;
            mPomodoroTimeLeft = TIMER_MAX_DURATION;
        }

        String minutesString = "";
        int buttonState = START;

        if (currentTime - startTime >= mPomodoroTimeLeft && mStartPauseState == START) {
            Log.d(TAG, "Loaded timer over");
            mPomodoroTimeLeft = 0;
            mStartPauseState = PAUSE;

            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
            remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
            buttonState = START;
            minutesString = this.getString(R.string.timer_zero);
        } else if (mStartPauseState == PAUSE) {
            Log.d(TAG, "Loaded timer paused");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
            remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
            buttonState = START;
            minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
        } else if (mStartPauseState == START) {
            Log.d(TAG, "Loaded timer started");
            mPomodoroTimeLeft = mPomodoroTimeLeft - (currentTime - startTime);
            Log.d(TAG, "Time left: " + mPomodoroTimeLeft);
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
            remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
            buttonState = PAUSE;
            minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
            startPomodoroTimer();
        } else if (mStartPauseState == STOP) {
            Log.d(TAG, "Loaded timer stopped");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
            remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_stop_button));
            buttonState = STOP;
            mPomodoroTimeLeft = 0;
            minutesString = this.getString(R.string.timer_zero);
        }

        broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
        broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, buttonState);

        remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);


        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private void startPomodoroTimer() {

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        setWidgetIntents(remoteViews);

        sPomodoroCountDownTimer = new CountDownTimer(mPomodoroTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mPomodoroTimeLeft = millisUntilFinished / 1000;
                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
            }

            @Override
            public void onFinish() {
                mPomodoroTimeLeft = 0;
                mStartPauseState = STOP;

                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, TimerService.this.getString(R.string.cd_stop_button));

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, STOP);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }

                mSharedPreferences.edit()
                        .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                        .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mPomodoroTimeLeft)
                        .apply();

                startAlarm();
            }
        }.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");

        mHasConfigurationChanged = true;
    }

    private void updateWidgetViews() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        setWidgetIntents(remoteViews);

        remoteViews.setTextViewText(R.id.ultradian_rhythm_status_text_view, Utility.formatUltradianTimeString(mMinutesLeft));
        remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

        switch (mStartPauseState) {
            case PAUSE:
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));
                break;
            case START:
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
                break;
            case STOP:
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_stop_button));
                break;
        }

        if (mRhythmState == WORK) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_work_white_36dp);
            remoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_work_icon));
        } else if (mRhythmState == REST) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            remoteViews.setContentDescription(R.id.work_rest_button, this.getString(R.string.cd_rest_icon));
        }

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }


    private void startAlarm() {
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void onStartPauseButtonClick() {
        Log.d(TAG, "Start/Pause timer");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        setWidgetIntents(remoteViews);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        try {
            if (mMediaPlayer.isPlaying()) {
                stopAlarm();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        switch (mStartPauseState) {
            case START:
                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));

                break;
            case PAUSE:
                mStartPauseState = START;

                if (mPomodoroTimeLeft == 0) {
                    mPomodoroTimeLeft = TIMER_MAX_DURATION;
                }

                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_pause_button));
                startPomodoroTimer();
                break;
            case STOP:
                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_start_arrow_white_24dp);
                remoteViews.setContentDescription(R.id.start_pause_button, this.getString(R.string.cd_start_button));

                mPomodoroTimeLeft = TIMER_MAX_DURATION;

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                String minutesString = Utility.formatPomodoroTimerString(mPomodoroTimeLeft);
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);

                break;

        }

        remoteViews.setTextViewText(R.id.ultradian_rhythm_status_text_view, Utility.formatUltradianTimeString(mMinutesLeft));
        remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimerString(mPomodoroTimeLeft));

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        mSharedPreferences.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mPomodoroTimeLeft)
                .apply();
    }

    public void onWorkRestButtonClick() {

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        if (mRhythmState == WORK) {
            mRhythmState = REST;
        } else if (mRhythmState == REST) {
            mRhythmState = WORK;
        }

        mSharedPreferences.edit()
                .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                .apply();

        startUltradianRhythmTimer();
    }


    private <T extends Serializable> void broadcastPomodoroMessage(String message, String key, T value) {
        if (!message.equals(POMODORO_EVENT_TIME_LEFT)) {
            Log.d(TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(POMODORO_EVENT);

        intent.putExtra(POMODORO_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private <T extends Serializable> void broadcastUltradianMessage(String message, String key, T value) {
        if (!message.equals(ULTRADIAN_EVENT_TIME_LEFT)) {
            Log.d(TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(ULTRADIAN_EVENT);

        intent.putExtra(ULTRADIAN_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToServiceHandler(Intent intent) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                Intent intent = (Intent) msg.obj;
                String action = intent.getAction();

                switch (action) {
                    case ACTION_START_SERVICE:
                        startUltradianRhythmTimer();
                        initializePomodoroTimer();
                        break;
                    case ACTION_ON_UPDATE:
                        mAppWidgetIds = intent.getIntArrayExtra(APP_WIDGET_IDS_KEY);
                        startUltradianRhythmTimer();
                        initializePomodoroTimer();
                        break;
                    case ACTION_START_PAUSE_TIMER:
                        onStartPauseButtonClick();
                        break;
                    case ACTION_WORK_REST_TIMER:
                        onWorkRestButtonClick();
                        break;
                    case ACTION_START_ULTRADIAN_TIMER:
                        startUltradianRhythmTimer();
                        break;
                    case ACTION_REQUEST_POMODORO_STATE:
                        switch (mStartPauseState) {
                            case START:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                                break;
                            case PAUSE:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                                break;
                            case STOP:
                                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, STOP);
                        }
                        broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mPomodoroTimeLeft);
                        break;
                    case ACTION_REQUEST_ULTRADIAN_STATE:
                        switch (mRhythmState) {
                            case WORK:
                                broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
                                break;
                            case REST:
                                broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, REST);
                                break;
                        }
                }
            }
        }
    }

}
