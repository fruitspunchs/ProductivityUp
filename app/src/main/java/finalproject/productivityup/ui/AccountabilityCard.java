package finalproject.productivityup.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import finalproject.productivityup.adapter.AccountabilityHoursCursorAdapter;
import finalproject.productivityup.adapter.OverviewAccountabilityHoursCursorAdapter;
import finalproject.productivityup.data.AccountabilityTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;

public class AccountabilityCard implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = MainActivity.CURSOR_LOADER_ID.ACCOUNTABILITY;
    private static CountDownTimer sNextDayCountdownTimer;
    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private final RecyclerView mRecyclerView;
    private final TextView mNoItemTextView;
    private OverviewAccountabilityHoursCursorAdapter mCursorAdapter;

    public AccountabilityCard(Context context, LoaderManager loaderManager, RecyclerView recyclerView, TextView noItemTextView) {
        mContext = context;
        mLoaderManager = loaderManager;
        mRecyclerView = recyclerView;
        mNoItemTextView = noItemTextView;
    }

    public void onCreate() {
        mLoaderManager.initLoader(CURSOR_LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        mCursorAdapter = new OverviewAccountabilityHoursCursorAdapter(mContext, null);
        mRecyclerView.setAdapter(mCursorAdapter);
    }

    public void onStart() {
        mLoaderManager.restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CURSOR_LOADER_ID) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            long currentTimeInSeconds = today.getTimeInMillis() / 1000;
            String[] selectionArgs = {String.valueOf(currentTimeInSeconds)};
            Log.d(LOG_TAG, "Loading date: " + selectionArgs[0]);

            return new CursorLoader(mContext, ProductivityProvider.AccountabilityChartTasks.CONTENT_URI,
                    null,
                    AccountabilityTasksColumns.DATE + " = ?",
                    selectionArgs,
                    AccountabilityTasksColumns.TIME + " ASC");
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CURSOR_LOADER_ID) {
            if (data.moveToNext()) {
                Log.d(LOG_TAG, "Has items");
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoItemTextView.setVisibility(View.GONE);

                int SECONDS_IN_HALF_HOUR = 1800;
                int SECONDS_IN_TWO_HOURS = 7200;

                //Create cursor to store time slots
                MatrixCursor timeSlotCursor = new MatrixCursor(new String[]{AccountabilityHoursCursorAdapter.AccountabilityHoursColumns._ID,
                        AccountabilityHoursCursorAdapter.AccountabilityHoursColumns.START});

                List<MatrixCursor> taskCursorList = new ArrayList<>();

                boolean firstItem = true;
                long threshold = 0;
                int timeSlotId = 0;
                int taskCursorListIndex = 0;

                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    long timeSpanStart = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));

                    if (firstItem) {
                        firstItem = false;

                        long round = timeSpanStart % SECONDS_IN_HALF_HOUR;
                        if (round >= SECONDS_IN_HALF_HOUR / 2) {
                            threshold = timeSpanStart + round;
                        } else {
                            threshold = timeSpanStart - round;
                        }

                        timeSlotCursor.addRow(new Object[]{timeSlotId++, threshold});
                        threshold += SECONDS_IN_TWO_HOURS;

                        MatrixCursor tmpMatrixCursor = new MatrixCursor(new String[]{AccountabilityTasksColumns._ID, AccountabilityTasksColumns.DATE, AccountabilityTasksColumns.TIME, AccountabilityTasksColumns.TASK});
                        taskCursorList.add(tmpMatrixCursor);
                        long id = data.getLong(data.getColumnIndex(AccountabilityTasksColumns._ID));
                        long date = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.DATE));
                        long time = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));
                        String task = data.getString(data.getColumnIndex(AccountabilityTasksColumns.TASK));

                        taskCursorList.get(taskCursorListIndex).addRow(new Object[]{id, date, time, task});

                    } else {
                        if (timeSpanStart >= threshold) {
                            while (timeSpanStart >= threshold) {
                                threshold += SECONDS_IN_TWO_HOURS;
                            }

                            timeSlotCursor.addRow(new Object[]{timeSlotId++, threshold - SECONDS_IN_TWO_HOURS});

                            taskCursorListIndex++;
                            MatrixCursor tmpMatrixCursor = new MatrixCursor(new String[]{AccountabilityTasksColumns._ID, AccountabilityTasksColumns.DATE, AccountabilityTasksColumns.TIME, AccountabilityTasksColumns.TASK});
                            taskCursorList.add(tmpMatrixCursor);
                        }

                        long id = data.getLong(data.getColumnIndex(AccountabilityTasksColumns._ID));
                        long date = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.DATE));
                        long time = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));
                        String task = data.getString(data.getColumnIndex(AccountabilityTasksColumns.TASK));

                        taskCursorList.get(taskCursorListIndex).addRow(new Object[]{id, date, time, task});
                    }
                }

                mCursorAdapter.swapCursor(timeSlotCursor);
                mCursorAdapter.setTaskCursorList(taskCursorList);

            } else {
                Log.d(LOG_TAG, "Has no items");
                mRecyclerView.setVisibility(View.GONE);
                mNoItemTextView.setVisibility(View.VISIBLE);
            }

            Calendar currentTime = Calendar.getInstance();

            Calendar tomorrow = Calendar.getInstance();
            tomorrow.set(Calendar.HOUR_OF_DAY, 0);
            tomorrow.set(Calendar.MINUTE, 0);
            tomorrow.set(Calendar.SECOND, 0);
            tomorrow.set(Calendar.MILLISECOND, 0);
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);

            long millisUntilTomorrow = tomorrow.getTimeInMillis() - currentTime.getTimeInMillis();
            Log.d(LOG_TAG, "Millis until tomorrow: " + millisUntilTomorrow);

            if (sNextDayCountdownTimer != null) {
                sNextDayCountdownTimer.cancel();
            }

            final AccountabilityCard thisClass = this;

            sNextDayCountdownTimer = new CountDownTimer(millisUntilTomorrow, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mLoaderManager.restartLoader(CURSOR_LOADER_ID, null, thisClass);
                }
            }.start();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CURSOR_LOADER_ID) {
            mCursorAdapter.swapCursor(null);
            mCursorAdapter.setTaskCursorList(null);
        }
    }
}