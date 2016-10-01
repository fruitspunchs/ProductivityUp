package io.github.fruitspunchs.productivityup.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.libs.Utility;
import io.github.fruitspunchs.productivityup.service.TimerService;

/**
 * Displays Pomodoro Timer State
 */
public class PomodoroTimerCard {
    public final static String POMODORO_TIMER_START_TIME_KEY = "POMODORO_TIMER_START_TIME_KEY";
    public final static String POMODORO_TIMER_START_PAUSE_KEY = "POMODORO_TIMER_START_PAUSE_KEY";
    public final static String POMODORO_TIMER_TIME_LEFT_KEY = "POMODORO_TIMER_TIME_LEFT_KEY";
    public final static int TIMER_MAX_DURATION = 25 * 60;
    public final static int START = 0;
    public final static int PAUSE = 1;
    public final static int STOP = 2;

    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final ImageButton mStartPauseImageButton;
    private final TextView mTimerTextView;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(TimerService.POMODORO_EVENT_KEY);
            if (!message.equals(TimerService.POMODORO_EVENT_TIME_LEFT)) {
                Log.d(LOG_TAG, "Got message: " + message);
            }

            switch (message) {
                case TimerService.POMODORO_EVENT_TIME_LEFT:
                    long timeLeft = intent.getLongExtra(TimerService.POMODORO_EVENT_TIME_LEFT_KEY, 0);
                    mTimerTextView.setText(Utility.formatPomodoroTimerString(timeLeft));
                    break;
                case TimerService.POMODORO_EVENT_BUTTON_STATE:
                    int timerState = intent.getIntExtra(TimerService.POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                    switch (timerState) {
                        case START:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_start_arrow_white_18dp);
                            mStartPauseImageButton.setContentDescription(mContext.getString(R.string.cd_start_button));
                            break;
                        case PAUSE:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_pause_white_18dp);
                            mStartPauseImageButton.setContentDescription(mContext.getString(R.string.cd_pause_button));
                            break;
                        case STOP:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_stop_white_18dp);
                            mStartPauseImageButton.setContentDescription(mContext.getString(R.string.cd_stop_button));
                            break;
                    }
            }
        }
    };

    public PomodoroTimerCard(final Context context, ImageButton startPauseButton, TextView timerTextView) {
        Log.d(LOG_TAG, "Pomodoro timer created");

        mContext = context;
        mStartPauseImageButton = startPauseButton;
        mTimerTextView = timerTextView;

        mStartPauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startPauseIntent = new Intent(context, TimerService.class);
                startPauseIntent.setAction(TimerService.ACTION_START_PAUSE_TIMER);
                context.startService(startPauseIntent);
            }
        });
    }

    public void onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter(TimerService.POMODORO_EVENT));
        Intent requestStateIntent = new Intent(mContext, TimerService.class);
        requestStateIntent.setAction(TimerService.ACTION_REQUEST_POMODORO_STATE);
        mContext.startService(requestStateIntent);
    }

    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
    }

}
