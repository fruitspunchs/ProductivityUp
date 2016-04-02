package finalproject.productivityup.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
    private final ImageButton mWorkRestImageButton;
    private final TextView mTimerTextView;
    private final String LOG_TAG = getClass().getSimpleName();
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
                    String minutesString = minutesLeft + mContext.getString(R.string.minute_letter);
                    mTimerTextView.setText(minutesString);
                    break;
                case TimerService.ULTRADIAN_EVENT_BUTTON_STATE:
                    int timerState = intent.getIntExtra(TimerService.ULTRADIAN_EVENT_BUTTON_STATE_KEY, WORK);
                    switch (timerState) {
                        case WORK:
                            mWorkRestImageButton.setImageResource(R.drawable.ic_work_white_36dp);
                            mWorkRestImageButton.setContentDescription(mContext.getString(R.string.cd_work_button));
                            break;
                        case REST:
                            mWorkRestImageButton.setImageResource(R.drawable.ic_break_white_36dp);
                            mWorkRestImageButton.setContentDescription(mContext.getString(R.string.cd_rest_button));
                            break;
                    }
            }

            if (mWorkRestImageButton.isFocused()) {
                ColorStateList colours = mWorkRestImageButton.getResources()
                        .getColorStateList(R.color.selector_gray_tint);
                Drawable d = DrawableCompat.wrap(mWorkRestImageButton.getDrawable());
                DrawableCompat.setTintList(d, colours);
                mWorkRestImageButton.setImageDrawable(d);
            }
        }
    };

    public UltradianRhythmTimerCard(final Context context, ImageButton workRestButton, TextView timerTextView) {
        mContext = context;
        mWorkRestImageButton = workRestButton;
        mTimerTextView = timerTextView;

        mWorkRestImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent workRestIntent = new Intent(context, TimerService.class);
                workRestIntent.setAction(TimerService.ACTION_WORK_REST_TIMER);
                context.startService(workRestIntent);
            }
        });

        Intent requestStateIntent = new Intent(mContext, TimerService.class);
        requestStateIntent.setAction(TimerService.ACTION_REQUEST_ULTRADIAN_STATE);
        mContext.startService(requestStateIntent);
    }

    public void onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter(TimerService.ULTRADIAN_EVENT));
    }

    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
    }
}
