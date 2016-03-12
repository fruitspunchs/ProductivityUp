package finalproject.productivityup.ui.deadlines;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.AnalyticsTrackedActivity;

public class AddDeadlineActivity extends AnalyticsTrackedActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.task_edit_text)
    EditText mTaskEditText;
    @Bind(R.id.add_deadline_done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.time_picker)
    TimePicker mTimePicker;
    @Bind(R.id.date_time_image_button)
    ImageButton mDateTimeButton;
    @Bind(R.id.datePicker)
    DatePicker mDatePicker;

    private int mMode = MODE.DATE;

    @OnClick(R.id.add_deadline_done_fab)
    void clickDoneFab() {
        String task = mTaskEditText.getText().toString().trim();
        Log.d(LOG_TAG, "Task number of trimmed chars: " + task.length());

        if (task.length() == 0) {
            Log.d(LOG_TAG, "No task input, returning");

            setResult(DeadlinesActivityFragment.RESULT_CANCEL);
            finish();

            return;
        }

        ContentValues values = new ContentValues();
        ContentValues deadlineDays = new ContentValues();

        Calendar calendar = Calendar.getInstance();
        calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0);

        long unixDate = calendar.getTimeInMillis() / 1000;
        Log.d(LOG_TAG, "UnixDate: " + unixDate);
        long unixHours = mTimePicker.getCurrentHour() * 3600;
        Log.d(LOG_TAG, "UnixHours: " + unixHours);
        long unixMinutes = mTimePicker.getCurrentMinute() * 60;
        Log.d(LOG_TAG, "UnixMinutes: " + unixMinutes);

        values.put(DeadlineTasksColumns.DATE, unixDate);
        Log.d(LOG_TAG, "Date: " + unixDate);
        values.put(DeadlineTasksColumns.TIME, unixDate + unixHours + unixMinutes);
        long time = unixDate + unixHours + unixMinutes;
        Log.d(LOG_TAG, "Time: " + time);
        values.put(DeadlineTasksColumns.TASK, task);
        Log.d(LOG_TAG, "Task: " + task);

        deadlineDays.put(DeadlineDaysColumns.DATE, unixDate);
        getContentResolver().insert(ProductivityProvider.DeadlineDays.CONTENT_URI, deadlineDays);

        getContentResolver().insert(ProductivityProvider.DeadlineTasks.CONTENT_URI, values);
        Intent resultIntent = new Intent().putExtra(DeadlinesActivityFragment.UNIX_DATE_KEY, unixDate);
        setResult(DeadlinesActivityFragment.RESULT_ADD, resultIntent);
        finish();
    }

    @OnClick(R.id.date_time_image_button)
    void clickDateTimeButton() {
        switch (mMode) {
            case MODE.DATE:
                mMode = MODE.TIME;
                mDateTimeButton.setImageResource(R.drawable.ic_event_white_48dp);
                mDateTimeButton.setContentDescription(this.getString(R.string.cd_select_date_button));
                mDatePicker.setVisibility(View.INVISIBLE);
                mTimePicker.setVisibility(View.VISIBLE);
                break;
            case MODE.TIME:
                mMode = MODE.DATE;
                mDateTimeButton.setImageResource(R.drawable.ic_alarm_white_48dp);
                mDateTimeButton.setContentDescription(this.getString(R.string.cd_select_time_button));
                mDatePicker.setVisibility(View.VISIBLE);
                mTimePicker.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @OnTextChanged(R.id.task_edit_text)
    void taskTextChanged() {
        if (mTaskEditText.getText().toString().trim().length() > 0) {
            mDoneFab.setImageResource(R.drawable.ic_done_white_48dp);
        } else {
            mDoneFab.setImageResource(R.drawable.ic_close_white_48dp);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deadline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);
    }

    private interface MODE {
        int DATE = 0;
        int TIME = 1;
    }

}
