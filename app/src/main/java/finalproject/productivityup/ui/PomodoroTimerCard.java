package finalproject.productivityup.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.service.TimerService;

/**
 * Created by User on 1/9/2016.
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
                    mTimerTextView.setText(Utility.formatPomodoroTimer(timeLeft));
                    break;
                case TimerService.POMODORO_EVENT_BUTTON_STATE:
                    int timerState = intent.getIntExtra(TimerService.POMODORO_EVENT_BUTTON_STATE_KEY, PAUSE);
                    switch (timerState) {
                        case START:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                            break;
                        case PAUSE:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_pause_white_24dp);
                            break;
                        case STOP:
                            mStartPauseImageButton.setImageResource(R.drawable.ic_stop_white_24dp);
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

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter(TimerService.POMODORO_EVENT));
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
    }

}
