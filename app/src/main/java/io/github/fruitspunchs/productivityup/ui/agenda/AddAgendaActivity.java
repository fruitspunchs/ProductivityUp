/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.agenda;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.data.AgendaTasksColumns;
import io.github.fruitspunchs.productivityup.data.DeadlineDaysColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;

public class AddAgendaActivity extends AppCompatActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.edit_text)
    EditText mTaskEditText;
    @Bind(R.id.add_agenda_done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.datePicker)
    DatePicker mDatePicker;

    @OnClick(R.id.add_agenda_done_fab)
    void clickDoneFab() {
        String task = mTaskEditText.getText().toString().trim();
        Log.d(LOG_TAG, "Task number of trimmed chars: " + task.length());

        if (task.length() == 0) {
            Log.d(LOG_TAG, "No task input, returning");
            setResult(AgendaActivityFragment.RESULT_CANCEL);
            finish();
            return;
        }

        ContentValues agendaTasks = new ContentValues();
        ContentValues agendaDays = new ContentValues();

        Calendar calendar = Calendar.getInstance();
        calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(), 0, 0, 0);

        long unixDate = calendar.getTimeInMillis() / 1000;
        Log.d(LOG_TAG, "UnixDate: " + unixDate);

        agendaTasks.put(AgendaTasksColumns.DATE, unixDate);
        Log.d(LOG_TAG, "Date: " + unixDate);
        agendaTasks.put(AgendaTasksColumns.TASK, task);
        Log.d(LOG_TAG, "Task: " + task);
        agendaTasks.put(AgendaTasksColumns.IS_CHECKED, 0);

        agendaDays.put(DeadlineDaysColumns.DATE, unixDate);

        getContentResolver().insert(ProductivityProvider.AgendaDays.CONTENT_URI, agendaDays);
        getContentResolver().insert(ProductivityProvider.AgendaTasks.CONTENT_URI, agendaTasks);

        Intent resultIntent = new Intent().putExtra(AgendaActivityFragment.UNIX_DATE_KEY, unixDate);
        setResult(AgendaActivityFragment.RESULT_ADD, resultIntent);
        finish();
    }

    @OnTextChanged(R.id.edit_text)
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
        setContentView(R.layout.activity_add_agenda);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);

        mTaskEditText.requestFocus();
    }

}
