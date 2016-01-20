package finalproject.productivityup.ui.deadlines;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
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

public class AddDeadlineActivity extends AppCompatActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.add_deadline_task_edit_text)
    EditText mTaskEditText;
    @Bind(R.id.add_deadline_done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.add_deadline_calendar_view)
    CalendarView mCalendarView;
    @Bind(R.id.add_deadline_time_picker)
    TimePicker mTimePicker;
    @Bind(R.id.add_deadline_date_time_image_button)
    ImageButton mDateTimeButton;
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

        long unixDate = mCalendarView.getDate() / 1000;
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

    @OnClick(R.id.add_deadline_date_time_image_button)
    void clickDateTimeButton() {
        switch (mMode) {
            case MODE.DATE:
                mMode = MODE.TIME;
                mDateTimeButton.setImageResource(R.drawable.ic_event_white_48dp);
                mCalendarView.setVisibility(View.INVISIBLE);
                mTimePicker.setVisibility(View.VISIBLE);
                break;
            case MODE.TIME:
                mMode = MODE.DATE;
                mDateTimeButton.setImageResource(R.drawable.ic_alarm_white_48dp);
                mCalendarView.setVisibility(View.VISIBLE);
                mTimePicker.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @OnTextChanged(R.id.add_deadline_task_edit_text)
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        //Set clock to start of day
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        mCalendarView.setDate(today.getTimeInMillis());

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                view.setDate(calendar.getTimeInMillis());
                Log.d(LOG_TAG, "Calendar set to: " + calendar.getTimeInMillis() / 1000);
            }
        });
    }

    private interface MODE {
        int DATE = 0;
        int TIME = 1;
    }

}
