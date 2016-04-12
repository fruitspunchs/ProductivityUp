package finalproject.productivityup.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.service.TimerService;

/**
 * Displays Ultradian Timer state
 */
public class UltradianRhythmTimerCard {

    public final static String ULTRADIAN_RHYTHM_START_TIME_KEY = "ULTRADIAN_RHYTHM_START_TIME_KEY";
    public final static String ULTRADIAN_RHYTHM_WORK_REST_KEY = "ULTRADIAN_RHYTHM_WORK_REST_KEY";
    public final static int WORK = 0;
    public final static int REST = 1;
    public final static int WORK_DURATION = 90 * 60;
    public final static int REST_DURATION = 30 * 60;
    private final Context mContext;
    private final TextView mTimerTextView;
    private final String LOG_TAG = getClass().getSimpleName();
    private String mWorkRestString;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(TimerService.ULTRADIAN_EVENT_KEY);
            if (!message.equals(TimerService.ULTRADIAN_EVENT_TIME_LEFT)) {
                Log.d(LOG_TAG, "Got message: " + message);
            }

            switch (message) {
                case TimerService.ULTRADIAN_EVENT_TIME_LEFT:
                    long minutesLeft = intent.getLongExtra(TimerService.ULTRADIAN_EVENT_TIME_LEFT_KEY, 0);
                    String displayString = minutesLeft + mContext.getString(R.string.minute_letter) + " " + mWorkRestString;
                    mTimerTextView.setText(displayString);
                    break;
                case TimerService.ULTRADIAN_EVENT_BUTTON_STATE:
                    int timerState = intent.getIntExtra(TimerService.ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
                    switch (timerState) {
                        case WORK:
                            mWorkRestString = mContext.getString(R.string.work);
                            break;
                        case REST:
                            mWorkRestString = mContext.getString(R.string.rest);
                            break;
                    }
            }
        }
    };

    public UltradianRhythmTimerCard(final Context context, TextView timerTextView) {
        mContext = context;
        mTimerTextView = timerTextView;
        mWorkRestString = mContext.getString(R.string.work);
    }

    public void onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter(TimerService.ULTRADIAN_EVENT));

        Intent requestStateIntent = new Intent(mContext, TimerService.class);
        requestStateIntent.setAction(TimerService.ACTION_REQUEST_ULTRADIAN_STATE);
        mContext.startService(requestStateIntent);
    }

    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
    }
}
