/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.accountability;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
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
import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.data.AccountabilityDaysColumns;
import io.github.fruitspunchs.productivityup.data.AccountabilityTasksColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;

public class EditAccountabilityActivity extends AppCompatActivity {

    public static final String TASK_KEY = "TASK_KEY";
    public static final String ID_KEY = "ID_KEY";
    public static final String DATE_KEY = "DATE_KEY";
    public static final String TIME_KEY = "TIME_KEY";
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.task_edit_text)
    EditText mTaskEditText;
    @Bind(R.id.done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.time_picker)
    TimePicker mTimePicker;
    @Bind(R.id.date_time_image_button)
    ImageButton mDateTimeButton;
    @Bind(R.id.datePicker)
    DatePicker mDatePicker;
    private int mMode = MODE.TIME;
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
    @OnClick(R.id.done_fab)
    void clickDoneFab() {
        String task = mTaskEditText.getText().toString().trim();
        Log.d(LOG_TAG, "Task number of trimmed chars: " + task.length());

        if (task.length() == 0) {
            Log.d(LOG_TAG, "No task input, returning");
            setResult(AccountabilityActivityFragment.RESULT_CANCEL);
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

        taskValues.put(AccountabilityTasksColumns.DATE, unixDate);
        Log.d(LOG_TAG, "Date: " + unixDate);
        taskValues.put(AccountabilityTasksColumns.TIME, unixDate + unixHours + unixMinutes);
        long time = unixDate + unixHours + unixMinutes;
        Log.d(LOG_TAG, "Time: " + time);
        taskValues.put(AccountabilityTasksColumns.TASK, task);
        Log.d(LOG_TAG, "Task: " + task);

        dateValues.put(AccountabilityDaysColumns.DATE, unixDate);
        getContentResolver().insert(ProductivityProvider.AccountabilityChartDays.CONTENT_URI, dateValues);

        String[] taskSelectionArgs = {String.valueOf(mId)};

        getContentResolver().update(ProductivityProvider.AccountabilityChartTasks.CONTENT_URI, taskValues, AccountabilityTasksColumns._ID + " = ?", taskSelectionArgs);

        // Delete date entry if there are no corresponding tasks
        String[] dateSelectionArgs = {String.valueOf(mDate)};
        Cursor cursor = getContentResolver().query(ProductivityProvider.AccountabilityChartTasks.CONTENT_URI, null, AccountabilityTasksColumns.DATE + " = ?", dateSelectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() < 1) {
                getContentResolver().delete(ProductivityProvider.AccountabilityChartDays.CONTENT_URI, AccountabilityTasksColumns.DATE + " = ?", dateSelectionArgs);
            }
            cursor.close();
        }

        Intent resultIntent = new Intent().putExtra(AccountabilityActivityFragment.UNIX_DATE_KEY, unixDate);
        setResult(AccountabilityActivityFragment.RESULT_EDIT, resultIntent);
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
        setContentView(R.layout.activity_add_accountability);
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

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mDate * 1000);

        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        mTaskEditText.setText(task);

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
