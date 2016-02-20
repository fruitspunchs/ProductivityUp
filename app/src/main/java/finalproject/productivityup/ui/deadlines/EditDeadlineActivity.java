package finalproject.productivityup.ui.deadlines;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import finalproject.productivityup.ui.MainActivity;

public class EditDeadlineActivity extends AppCompatActivity {
    public static final String TASK_KEY = "TASK_KEY";
    public static final String ID_KEY = "ID_KEY";
    public static final String DATE_KEY = "DATE_KEY";
    public static final String TIME_KEY = "TIME_KEY";
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.task_edit_text)
    EditText mTaskEditText;
    @Bind(R.id.add_deadline_done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.calendar_view)
    CalendarView mCalendarView;
    @Bind(R.id.time_picker)
    TimePicker mTimePicker;
    @Bind(R.id.date_time_image_button)
    ImageButton mDateTimeButton;
    private int mMode = MODE.DATE;
    private long mId;
    private long mDate;

    @SuppressWarnings("deprecation")
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

        ContentValues taskValues = new ContentValues();
        ContentValues dateValues = new ContentValues();

        long unixDate = mCalendarView.getDate() / 1000;
        Log.d(LOG_TAG, "UnixDate: " + unixDate);

        long unixHours = mTimePicker.getCurrentHour() * 3600;
        Log.d(LOG_TAG, "UnixHours: " + unixHours);
        long unixMinutes = mTimePicker.getCurrentMinute() * 60;
        Log.d(LOG_TAG, "UnixMinutes: " + unixMinutes);

        taskValues.put(DeadlineTasksColumns.DATE, unixDate);
        Log.d(LOG_TAG, "Date: " + unixDate);
        taskValues.put(DeadlineTasksColumns.TIME, unixDate + unixHours + unixMinutes);
        long time = unixDate + unixHours + unixMinutes;
        Log.d(LOG_TAG, "Time: " + time);
        taskValues.put(DeadlineTasksColumns.TASK, task);
        Log.d(LOG_TAG, "Task: " + task);

        dateValues.put(DeadlineDaysColumns.DATE, unixDate);
        getContentResolver().insert(ProductivityProvider.DeadlineDays.CONTENT_URI, dateValues);

        String[] taskSelectionArgs = {String.valueOf(mId)};

        getContentResolver().update(ProductivityProvider.DeadlineTasks.CONTENT_URI, taskValues, DeadlineTasksColumns._ID + " = ?", taskSelectionArgs);

        // Delete date entry if there are no corresponding tasks
        String[] dateSelectionArgs = {String.valueOf(mDate)};
        Cursor cursor = getContentResolver().query(ProductivityProvider.DeadlineTasks.CONTENT_URI, null, DeadlineTasksColumns.DATE + " = ?", dateSelectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() < 1) {
                getContentResolver().delete(ProductivityProvider.DeadlineDays.CONTENT_URI, DeadlineDaysColumns.DATE + " = ?", dateSelectionArgs);
            }
            cursor.close();
        }

        Intent resultIntent = new Intent().putExtra(DeadlinesActivityFragment.UNIX_DATE_KEY, unixDate);
        setResult(DeadlinesActivityFragment.RESULT_EDIT, resultIntent);
        finish();
    }

    @OnClick(R.id.date_time_image_button)
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

        mDoneFab.setImageResource(R.drawable.ic_done_white_48dp);


        mId = getIntent().getLongExtra(ID_KEY, -1);
        String task = getIntent().getStringExtra(TASK_KEY);

        mDate = getIntent().getLongExtra(DATE_KEY, -1);
        long time = getIntent().getLongExtra(TIME_KEY, -1);

        mTaskEditText.setText(task);
        mCalendarView.setMinDate(MainActivity.CALENDAR_MIN_DATE);
        mCalendarView.setDate(mDate * 1000);

        long daySeconds = time - mDate;

        int hour = 0;
        int minute = 0;

        while (daySeconds >= 3600) {
            hour += 1;
            daySeconds -= 3600;
        }

        while (daySeconds >= 60) {
            minute += 1;
            daySeconds -= 60;
        }

        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);

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
