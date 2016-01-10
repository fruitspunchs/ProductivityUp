package finalproject.productivityup.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;

/**
 * Created by User on 1/9/2016.
 */
public class UltradianRhythmTimer {

    private final static String ULTRADIAN_RHYTHM_START_TIME_KEY = "ULTRADIAN_RHYTHM_START_TIME_KEY";
    private final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = "ULTRADIAN_RHYTHM_WORK_REST_KEY";
    private final static int WORK = 0;
    private final static int REST = 1;
    private final static int WORK_DURATION = 90 * 60;
    private final static int REST_DURATION = 30 * 60;
    private final Context mContext;
    private final ImageButton mWorkRestImageButton;
    private final TextView mTimerTextView;
    private CountDownTimer mCountDownTimer;
    private int mRhythmState;

    public UltradianRhythmTimer(Context context, ImageButton workRestButton, TextView timerTextView) {
        mContext = context;
        mWorkRestImageButton = workRestButton;
        mTimerTextView = timerTextView;
    }

    public long getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public void startTimer() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        long startTime;

        if (prefs.contains(ULTRADIAN_RHYTHM_START_TIME_KEY)) {
            startTime = prefs.getLong(ULTRADIAN_RHYTHM_START_TIME_KEY, 0);
            mRhythmState = prefs.getInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, WORK);
        } else {
            startTime = getCurrentTimeInSeconds();
            mRhythmState = WORK;
        }

        long timeElapsed = startTime - getCurrentTimeInSeconds();

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
            mWorkRestImageButton.setImageResource(R.drawable.ic_work_white_36dp);
            timeLeft = WORK_DURATION - timeElapsed;
        } else if (mRhythmState == REST) {
            mWorkRestImageButton.setImageResource(R.drawable.ic_break_white_36dp);
            timeLeft = REST_DURATION - timeElapsed;
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / (1000 * 60);
                String minutesString = minutesLeft + mContext.getString(R.string.minute_letter);
                mTimerTextView.setText(minutesString);
            }

            @Override
            public void onFinish() {
                if (mRhythmState == WORK) {
                    mRhythmState = REST;
                } else if (mRhythmState == REST) {
                    mRhythmState = WORK;
                }

                prefs.edit()
                        .putLong(ULTRADIAN_RHYTHM_START_TIME_KEY, System.currentTimeMillis())
                        .putInt(ULTRADIAN_RHYTHM_WORK_REST_KEY, mRhythmState)
                        .commit();

                startTimer();
            }
        }.start();
    }
}
