package finalproject.productivityup.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
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

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.PomodoroTimerCard;
import finalproject.productivityup.ui.UltradianRhythmTimerCard;
import finalproject.productivityup.widget.TimerAppWidgetProvider;

/**
 * Created by User on 1/11/2016.
 */

public class TimerService extends Service {
    public final static String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public final static String ACTION_ON_UPDATE = "ACTION_ON_UPDATE";
    public final static String ACTION_START_PAUSE_TIMER = "ACTION_START_PAUSE_TIMER";
    public final static String ACTION_WORK_REST_TIMER = "ACTION_WORK_REST_TIMER";
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

    private final String LOG_TAG = getClass().getSimpleName();

    private int mRhythmState;
    private int[] mAppWidgetIds;
    private int mStartPauseState;
    private long mTimeLeft;
    private SharedPreferences mSharedPreferences;
    private String mTimerZeroString;

    private MediaPlayer mMediaPlayer;
    private ServiceHandler mServiceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("TimerThread");
        thread.start();

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mTimerZeroString = this.getString(R.string.timer_zero);
        mAppWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this.getPackageName(), TimerAppWidgetProvider.class.getName()));

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

    public void startUltradianRhythmTimer() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

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
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            remoteViews.setImageViewResource(R.id.work_rest_button, R.drawable.ic_break_white_36dp);
            broadcastUltradianMessage(ULTRADIAN_EVENT_BUTTON_STATE, ULTRADIAN_EVENT_BUTTON_STATE_KEY, REST);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        sUltradianRhythmCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / (1000 * 60);

                String prefix = "";

                if (minutesLeft < 10) {
                    prefix = "0";
                }

                String minutesString = prefix + minutesLeft;

                broadcastUltradianMessage(ULTRADIAN_EVENT_TIME_LEFT, ULTRADIAN_EVENT_TIME_LEFT_KEY, minutesLeft);
                remoteViews.setTextViewText(R.id.ultradian_rhythm_timer_text_view, minutesString);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }
            }

            @Override
            public void onFinish() {
                if (mRhythmState == WORK) {
                    mRhythmState = REST;
                } else if (mRhythmState == REST) {
                    mRhythmState = WORK;
                }
                startUltradianRhythmTimer();
            }
        }.start();

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    public void initializePomodoroTimer() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        Log.d(LOG_TAG, "Pomodoro timer initialized");

        long startTime;
        long currentTime = Utility.getCurrentTimeInSeconds();
        if (mSharedPreferences.contains(POMODORO_TIMER_START_TIME_KEY)) {
            Log.d(LOG_TAG, "Found preferences");
            startTime = mSharedPreferences.getLong(POMODORO_TIMER_START_TIME_KEY, currentTime);
            mStartPauseState = mSharedPreferences.getInt(POMODORO_TIMER_START_PAUSE_KEY, START);
            mTimeLeft = mSharedPreferences.getLong(POMODORO_TIMER_TIME_LEFT_KEY, 0);
        } else {
            Log.d(LOG_TAG, "No preferences");
            startTime = currentTime;
            mStartPauseState = PAUSE;
            mTimeLeft = TIMER_MAX_DURATION;
        }

        String minutesString = "";
        int buttonState = START;

        if (currentTime - startTime >= mTimeLeft && mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer over");
            mTimeLeft = 0;
            mStartPauseState = PAUSE;

            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);
            buttonState = START;
            minutesString = this.getString(R.string.timer_zero);
        } else if (mStartPauseState == PAUSE) {
            Log.d(LOG_TAG, "Loaded timer paused");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);
            buttonState = START;
            minutesString = Utility.formatPomodoroTimer(mTimeLeft);
        } else if (mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer started");
            mTimeLeft = mTimeLeft - (currentTime - startTime);
            Log.d(LOG_TAG, "Time left: " + mTimeLeft);
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
            buttonState = PAUSE;
            minutesString = Utility.formatPomodoroTimer(mTimeLeft);
            startPomodoroTimer();
        } else if (mStartPauseState == STOP) {
            Log.d(LOG_TAG, "Loaded timer stopped");
            remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
            buttonState = STOP;
            mTimeLeft = 0;
            minutesString = this.getString(R.string.timer_zero);
        }

        broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mTimeLeft);
        broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, buttonState);

        remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private void startPomodoroTimer() {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        sPomodoroCountDownTimer = new CountDownTimer(mTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, Utility.formatPomodoroTimer(millisUntilFinished / 1000));
                mTimeLeft = millisUntilFinished / 1000;

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mTimeLeft);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }
            }

            @Override
            public void onFinish() {
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, mTimerZeroString);

                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_stop_white_24dp);
                mTimeLeft = 0;
                mStartPauseState = STOP;

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mTimeLeft);
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, STOP);

                for (int appWidgetId : mAppWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }

                startAlarm();
            }
        }.start();
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
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.timer_appwidget);
        Log.d(LOG_TAG, "Start/Pause timer");

        if (sPomodoroCountDownTimer != null) {
            sPomodoroCountDownTimer.cancel();
        }

        switch (mStartPauseState) {
            case START:
                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);

                break;
            case PAUSE:
                mStartPauseState = START;

                if (mTimeLeft == 0) {
                    mTimeLeft = TIMER_MAX_DURATION;
                }

                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_pause_white_24dp);
                startPomodoroTimer();
                break;
            case STOP:
                stopAlarm();

                mStartPauseState = PAUSE;
                broadcastPomodoroMessage(POMODORO_EVENT_BUTTON_STATE, POMODORO_EVENT_BUTTON_STATE_KEY, START);
                remoteViews.setImageViewResource(R.id.start_pause_button, R.drawable.ic_play_arrow_white_24dp);

                mTimeLeft = TIMER_MAX_DURATION;

                broadcastPomodoroMessage(POMODORO_EVENT_TIME_LEFT, POMODORO_EVENT_TIME_LEFT_KEY, mTimeLeft);
                String minutesString = Utility.formatPomodoroTimer(mTimeLeft);
                remoteViews.setTextViewText(R.id.pomodoro_timer_text_view, minutesString);

                break;

        }

        for (int appWidgetId : mAppWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        mSharedPreferences.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                .apply();
    }

    public void onWorkRestButtonClick() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sUltradianRhythmCountDownTimer != null) {
            sUltradianRhythmCountDownTimer.cancel();
        }

        if (mRhythmState == WORK) {
            mRhythmState = REST;
        } else if (mRhythmState == REST) {
            mRhythmState = WORK;
        }

        prefs.edit()
                .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                .apply();

        startUltradianRhythmTimer();
    }


    private <T extends Serializable> void broadcastPomodoroMessage(String message, String key, T value) {
        if (!message.equals(POMODORO_EVENT_TIME_LEFT)) {
            Log.d(LOG_TAG, "Broadcasting message: " + message);
        }

        Intent intent = new Intent(POMODORO_EVENT);

        intent.putExtra(POMODORO_EVENT_KEY, message);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private <T extends Serializable> void broadcastUltradianMessage(String message, String key, T value) {
        if (!message.equals(ULTRADIAN_EVENT_TIME_LEFT)) {
            Log.d(LOG_TAG, "Broadcasting message: " + message);
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
                }
            }
        }
    }

}