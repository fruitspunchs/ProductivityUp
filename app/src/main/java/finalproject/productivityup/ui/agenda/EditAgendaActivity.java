package finalproject.productivityup.ui.agenda;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import finalproject.productivityup.R;
import finalproject.productivityup.data.AgendaDaysColumns;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.ProductivityProvider;

public class EditAgendaActivity extends AppCompatActivity {
    public static final String TASK_KEY = "TASK_KEY";
    public static final String ID_KEY = "ID_KEY";
    public static final String DATE_KEY = "DATE_KEY";
    public static final String CHECK_VALUE_KEY = "CHECK_VALUE_KEY";

    private final String LOG_TAG = getClass().getSimpleName();
    @Bind(R.id.edit_text)
    EditText mTaskEditText;
    @Bind(R.id.add_agenda_done_fab)
    FloatingActionButton mDoneFab;
    @Bind(R.id.datePicker)
    DatePicker mDatePicker;

    private long mId;
    private long mDate;
    private int mCheckValue;

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
        agendaTasks.put(AgendaTasksColumns.IS_CHECKED, mCheckValue);

        agendaDays.put(DeadlineDaysColumns.DATE, unixDate);

        String[] taskSelectionArgs = {String.valueOf(mId)};
        getContentResolver().update(ProductivityProvider.AgendaTasks.CONTENT_URI, agendaTasks, AgendaTasksColumns._ID + " = ?", taskSelectionArgs);
        getContentResolver().insert(ProductivityProvider.AgendaDays.CONTENT_URI, agendaDays);

        // Delete date entry if there are no corresponding tasks
        String[] dateSelectionArgs = {String.valueOf(mDate)};
        Cursor cursor = getContentResolver().query(ProductivityProvider.AgendaTasks.CONTENT_URI, null, AgendaTasksColumns.DATE + " = ?", dateSelectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() < 1) {
                getContentResolver().delete(ProductivityProvider.AgendaDays.CONTENT_URI, AgendaDaysColumns.DATE + " = ?", dateSelectionArgs);
            }
            cursor.close();
        }

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
        setContentView(R.layout.activity_edit_agenda);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.bind(this);

        mId = getIntent().getLongExtra(ID_KEY, -1);
        String task = getIntent().getStringExtra(TASK_KEY);
        mDate = getIntent().getLongExtra(DATE_KEY, -1);
        mCheckValue = getIntent().getIntExtra(CHECK_VALUE_KEY, -1);

        mTaskEditText.setText(task);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mDate * 1000);

        mDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        mTaskEditText.requestFocus();
    }

}
