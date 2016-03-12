package finalproject.productivityup.ui.deadlines;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.AnalyticsTrackedActivity;

public class EditDeadlineActivity extends AnalyticsTrackedActivity {
    public static final String TASK_KEY = "TASK_KEY";
    public static final String ID_KEY = "ID_KEY";
    public static final String DATE_KEY = "DATE_KEY";
    public static final String TIME_KEY = "TIME_KEY";
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
    private long mId;
    private long mDate;

    @OnFocusChange(R.id.date_time_image_button)
    void onDateTimeButtonFocusChange() {
        ColorStateList colours = mDateTimeButton.getResources()
                .getColorStateList(R.color.selector_accent_tint);
        Drawable d = DrawableCompat.wrap(mDateTimeButton.getDrawable());
        DrawableCompat.setTintList(d, colours);
        mDateTimeButton.setImageDrawable(d);
    }

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

        Calendar calendar = Calendar.getInstance();
        calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0);

        long unixDate = calendar.getTimeInMillis() / 1000;
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

        if (mDateTimeButton.isFocused()) {
            ColorStateList colours = mDateTimeButton.getResources()
                    .getColorStateList(R.color.selector_gray_tint);
            Drawable d = DrawableCompat.wrap(mDateTimeButton.getDrawable());
            DrawableCompat.setTintList(d, colours);
            mDateTimeButton.setImageDrawable(d);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mDate * 1000);

        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

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

        mTaskEditText.requestFocus();
    }

    private interface MODE {
        int DATE = 0;
        int TIME = 1;
    }

}
