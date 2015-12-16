package finalproject.productivityup;

import android.content.ContentValues;
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
import finalproject.productivityup.data.DeadlinesColumns;
import finalproject.productivityup.data.ProductivityProvider;

public class AddDeadlineActivity extends AppCompatActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.add_deadline_task_edit_text)
    EditText taskEditText;
    @Bind(R.id.add_deadline_done_fab)
    FloatingActionButton doneFab;
    @Bind(R.id.add_deadline_calendar_view)
    CalendarView calendarView;
    @Bind(R.id.add_deadline_time_picker)
    TimePicker timePicker;
    @Bind(R.id.add_deadline_date_time_image_button)
    ImageButton dateTimeButton;
    private int mode = MODE.DATE;

    @OnClick(R.id.add_deadline_done_fab)
    void clickDoneFab() {
        String task = taskEditText.getText().toString().trim();

        if (task.length() == 0)
            finish();

        ContentValues values = new ContentValues();
        //TODO
        long unixDate = calendarView.getDate() / 1000;
        Log.d(LOG_TAG, "UnixDate: " + unixDate);
        long unixHours = timePicker.getCurrentHour() * 3600;
        Log.d(LOG_TAG, "UnixHours: " + unixHours);
        long unixMinutes = timePicker.getCurrentMinute() * 60;
        Log.d(LOG_TAG, "UnixMinutes: " + unixMinutes);

        long date = unixDate + unixHours + unixMinutes;

        values.put(DeadlinesColumns.DATE, date);
        Log.d(LOG_TAG, "Date: " + date);
        values.put(DeadlinesColumns.TASK, task);
        Log.d(LOG_TAG, "Task: " + task);
        getContentResolver().insert(ProductivityProvider.Deadlines.CONTENT_URI, values);
        finish();
    }

    @OnClick(R.id.add_deadline_date_time_image_button)
    void clickDateTimeButton() {
        switch (mode) {
            case MODE.DATE:
                mode = MODE.TIME;
                dateTimeButton.setImageResource(R.drawable.ic_event_white_48dp);
                calendarView.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                break;
            case MODE.TIME:
                mode = MODE.DATE;
                dateTimeButton.setImageResource(R.drawable.ic_alarm_white_48dp);
                calendarView.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @OnTextChanged(R.id.add_deadline_task_edit_text)
    void taskTextChanged() {
        if (taskEditText.getText().toString().trim().length() > 0) {
            doneFab.setImageResource(R.drawable.ic_done_white_48dp);
        } else {
            doneFab.setImageResource(R.drawable.ic_close_white_48dp);
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
        calendarView.setDate(today.getTimeInMillis());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
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
