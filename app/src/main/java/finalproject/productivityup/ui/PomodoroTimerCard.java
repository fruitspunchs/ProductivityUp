package finalproject.productivityup.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.service.AlarmPlayerService;

/**
 * Created by User on 1/9/2016.
 */
public class PomodoroTimerCard {
    private final static String POMODORO_TIMER_START_TIME_KEY = "POMODORO_TIMER_START_TIME_KEY";
    private final static String POMODORO_TIMER_START_PAUSE_KEY = "POMODORO_TIMER_START_PAUSE_KEY";
    private final static String POMODORO_TIMER_TIME_LEFT_KEY = "POMODORO_TIMER_TIME_LEFT_KEY";
    private final static int TIMER_MAX_DURATION = 25 * 60;
    private final static int START = 0;
    private final static int PAUSE = 1;
    private final static int STOP = 2;
    private static CountDownTimer sCountDownTimer;
    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final ImageButton mWorkRestImageButton;
    private final TextView mTimerTextView;
    private int mStartPauseState;
    private long mTimeLeft;
    private SharedPreferences mSharedPreferences;

    public PomodoroTimerCard(Context context, ImageButton startPauseButton, TextView timerTextView) {
        Log.d(LOG_TAG, "Pomodoro timer created");

        mContext = context;
        mWorkRestImageButton = startPauseButton;
        mTimerTextView = timerTextView;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mWorkRestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sCountDownTimer != null) {
                    sCountDownTimer.cancel();
                }

                switch (mStartPauseState) {
                    case START:
                        mStartPauseState = PAUSE;
                        mWorkRestImageButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                        break;
                    case PAUSE:
                        mStartPauseState = START;

                        if (mTimeLeft == 0) {
                            mTimeLeft = TIMER_MAX_DURATION;
                        }

                        mWorkRestImageButton.setImageResource(R.drawable.ic_pause_white_24dp);
                        startTimer();
                        break;
                    case STOP:
                        stopAlarm();

                        mStartPauseState = PAUSE;
                        mWorkRestImageButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                        mTimeLeft = TIMER_MAX_DURATION;

                        String minutesString = formatTime(mTimeLeft);
                        mTimerTextView.setText(minutesString);

                        break;

                }

                mSharedPreferences.edit()
                        .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                        .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                        .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                        .apply();
            }
        });
    }

    private void startAlarm() {
        Intent intent = new Intent(mContext, AlarmPlayerService.class);
        intent.setAction(AlarmPlayerService.ALARM_PLAYER_START);
        mContext.startService(intent);
    }

    private void stopAlarm() {
        Intent intent = new Intent(mContext, AlarmPlayerService.class);
        intent.setAction(AlarmPlayerService.ALARM_PLAYER_STOP);
        mContext.startService(intent);
    }

    public void initialize() {
        if (sCountDownTimer != null) {
            sCountDownTimer.cancel();
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

        if (currentTime - startTime >= mTimeLeft && mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer over");
            mTimeLeft = 0;
            mStartPauseState = PAUSE;
            mWorkRestImageButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            String minutesString = mContext.getString(R.string.timer_zero);
            mTimerTextView.setText(minutesString);
        } else if (mStartPauseState == PAUSE) {
            Log.d(LOG_TAG, "Loaded timer paused");
            mWorkRestImageButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            String minutesString = formatTime(mTimeLeft);
            mTimerTextView.setText(minutesString);
        } else if (mStartPauseState == START) {
            Log.d(LOG_TAG, "Loaded timer started");
            mTimeLeft = mTimeLeft - (currentTime - startTime);
            Log.d(LOG_TAG, "Time left: " + mTimeLeft);
            mWorkRestImageButton.setImageResource(R.drawable.ic_pause_white_24dp);
            String minutesString = formatTime(mTimeLeft);
            mTimerTextView.setText(minutesString);
            startTimer();
        } else if (mStartPauseState == STOP) {
            Log.d(LOG_TAG, "Loaded timer stopped");
            mWorkRestImageButton.setImageResource(R.drawable.ic_stop_white_24dp);
            mTimeLeft = 0;
            String minutesString = mContext.getString(R.string.timer_zero);
            mTimerTextView.setText(minutesString);
        }
    }

    public void onPause() {
        mSharedPreferences.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, Utility.getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                .apply();
    }

    private void startTimer() {
        if (sCountDownTimer != null) {
            sCountDownTimer.cancel();
        }

        sCountDownTimer = new CountDownTimer(mTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimerTextView.setText(formatTime(millisUntilFinished / 1000));
                mTimeLeft = millisUntilFinished / 1000;
            }

            @Override
            public void onFinish() {
                mTimerTextView.setText(R.string.timer_zero);
                mWorkRestImageButton.setImageResource(R.drawable.ic_stop_white_24dp);
                mTimeLeft = 0;
                mStartPauseState = STOP;

                startAlarm();
            }
        }.start();
    }

    private String formatTime(long seconds) {
        long minutesLeft = seconds / 60;
        long secondsLeft = seconds % 60;
        String leadingZero = "";
        if (secondsLeft < 10) {
            leadingZero = "0";
        }
        return minutesLeft + ":" + leadingZero + secondsLeft;

    }
}
