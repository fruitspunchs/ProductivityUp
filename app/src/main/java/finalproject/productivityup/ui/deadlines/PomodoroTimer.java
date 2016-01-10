package finalproject.productivityup.ui.deadlines;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;

/**
 * Created by User on 1/9/2016.
 */
public class PomodoroTimer {

    private final static String POMODORO_TIMER_START_TIME_KEY = "POMODORO_TIMER_START_TIME_KEY";
    private final static String POMODORO_TIMER_START_PAUSE_KEY = "POMODORO_TIMER_START_PAUSE_KEY";
    private final static String POMODORO_TIMER_TIME_LEFT_KEY = "POMODORO_TIMER_TIME_LEFT_KEY";
    private final static int TIMER_MAX_DURATION = 25 * 60;
    private final static int START = 0;
    private final static int PAUSE = 1;
    private final Context mContext;
    private final ImageButton mWorkRestImageButton;
    private final TextView mTimerTextView;
    private CountDownTimer mCountDownTimer;
    private int mStartPauseState;
    private long mTimeLeft;
    private long mStartTime;

    public PomodoroTimer(Context context, ImageButton startPauseButton, TextView timerTextView) {
        mContext = context;
        mWorkRestImageButton = startPauseButton;
        mTimerTextView = timerTextView;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (prefs.contains(POMODORO_TIMER_START_TIME_KEY)) {
            mStartTime = prefs.getLong(POMODORO_TIMER_START_TIME_KEY, getCurrentTimeInSeconds());
            mStartPauseState = prefs.getInt(POMODORO_TIMER_START_PAUSE_KEY, START);
            mTimeLeft = prefs.getInt(POMODORO_TIMER_TIME_LEFT_KEY, 0);
        } else {
            mStartTime = getCurrentTimeInSeconds();
            mStartPauseState = START;
            mTimeLeft = TIMER_MAX_DURATION;
            prefs.edit()
                    .putLong(POMODORO_TIMER_START_TIME_KEY, getCurrentTimeInSeconds())
                    .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                    .apply();
        }

        if (getCurrentTimeInSeconds() - mStartTime >= TIMER_MAX_DURATION && mStartPauseState == START) {
            mTimeLeft = TIMER_MAX_DURATION;
            mStartPauseState = PAUSE;
            mWorkRestImageButton.setImageResource(R.drawable.ic_play_circle_filled_white_36dp);
            String minutesString = mTimeLeft + mContext.getString(R.string.minute_letter);
            mTimerTextView.setText(minutesString);
        } else if (mStartPauseState == PAUSE) {
            mWorkRestImageButton.setImageResource(R.drawable.ic_play_circle_filled_white_36dp);
            String minutesString = mTimeLeft + mContext.getString(R.string.minute_letter);
            mTimerTextView.setText(minutesString);
        } else {
            mWorkRestImageButton.setImageResource(R.drawable.ic_pause_circle_filled_white_36dp);
            startTimer();
        }


        mWorkRestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                }

                switch (mStartPauseState) {
                    case START:
                        mStartPauseState = PAUSE;
                        mWorkRestImageButton.setImageResource(R.drawable.ic_play_circle_filled_white_36dp);
                        break;
                    case PAUSE:
                        mStartPauseState = START;
                        mTimeLeft = TIMER_MAX_DURATION;
                        mWorkRestImageButton.setImageResource(R.drawable.ic_pause_circle_filled_white_36dp);
                        startTimer();
                        break;
                }

                prefs.edit()
                        .putLong(POMODORO_TIMER_START_TIME_KEY, getCurrentTimeInSeconds())
                        .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                        .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                        .apply();
            }
        });
    }

    public long getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public void onActivityPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putLong(POMODORO_TIMER_START_TIME_KEY, getCurrentTimeInSeconds())
                .putInt(POMODORO_TIMER_START_PAUSE_KEY, mStartPauseState)
                .putLong(POMODORO_TIMER_TIME_LEFT_KEY, mTimeLeft)
                .apply();
    }

    private void startTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(mTimeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / (1000 * 60);
                String minutesString = minutesLeft + mContext.getString(R.string.minute_letter);
                mTimerTextView.setText(minutesString);
                mTimeLeft = minutesLeft;
            }

            @Override
            public void onFinish() {
                mTimerTextView.setText(R.string.time_up);
                mWorkRestImageButton.setImageResource(R.drawable.ic_play_circle_filled_white_36dp);
                mTimeLeft = 25;
                mStartPauseState = PAUSE;
            }
        }.start();
    }
}
