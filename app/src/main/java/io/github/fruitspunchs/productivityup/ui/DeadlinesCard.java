/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.adapter.OverviewDeadlinesCursorAdapter;
import io.github.fruitspunchs.productivityup.data.DeadlineTasksColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;
import io.github.fruitspunchs.productivityup.libs.Utility;

public class DeadlinesCard implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASKS_CURSOR_LOADER_ID = MainActivity.CURSOR_LOADER_ID.DEADLINE_TASKS;
    private static final int NEXT_DEADLINE_CURSOR_LOADER_ID = MainActivity.CURSOR_LOADER_ID.NEXT_DEADLINE;
    private static CountDownTimer sDeadlinesCountdownTimer;
    private static CountDownTimer sDeadlineTimeUpDelayCountDownTimer;
    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private final RecyclerView mRecyclerView;
    private final TextView mNoItemTextView;
    private final TextView mTimeLeftTextView;
    private OverviewDeadlinesCursorAdapter mCursorAdapter;
    private long mNextDeadlineUnixTime = -1;

    public DeadlinesCard(Context context, LoaderManager loaderManager, RecyclerView recyclerView, TextView timeLeftTextView, TextView noItemTextView) {
        mContext = context;
        mLoaderManager = loaderManager;
        mRecyclerView = recyclerView;
        mTimeLeftTextView = timeLeftTextView;
        mNoItemTextView = noItemTextView;
    }

    public void onCreate() {
        mLoaderManager.initLoader(TASKS_CURSOR_LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mCursorAdapter = new OverviewDeadlinesCursorAdapter(mContext, null);
        mRecyclerView.setAdapter(mCursorAdapter);
    }

    public void onStart() {
        mLoaderManager.restartLoader(TASKS_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TASKS_CURSOR_LOADER_ID) {
            Calendar currentTime = Calendar.getInstance();
            long currentTimeInSeconds = currentTime.getTimeInMillis() / 1000;
            String[] selectionArgs = {String.valueOf(currentTimeInSeconds)};

            return new CursorLoader(mContext, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " > ?",
                    selectionArgs,
                    DeadlineTasksColumns.TIME + " ASC");
        } else if (id == NEXT_DEADLINE_CURSOR_LOADER_ID) {
            String[] selectionArgs = {String.valueOf(mNextDeadlineUnixTime)};

            return new CursorLoader(mContext, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " = ?",
                    selectionArgs,
                    null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == NEXT_DEADLINE_CURSOR_LOADER_ID) {
            mCursorAdapter.swapCursor(data);
        } else if (loader.getId() == TASKS_CURSOR_LOADER_ID) {
            if (data.moveToNext()) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoItemTextView.setVisibility(View.GONE);

                mNextDeadlineUnixTime = data.getLong(data.getColumnIndex(DeadlineTasksColumns.TIME));

                long timeUntilNextDeadline = mNextDeadlineUnixTime * 1000 - System.currentTimeMillis();

                if (null != sDeadlinesCountdownTimer) {
                    sDeadlinesCountdownTimer.cancel();
                }

                sDeadlinesCountdownTimer = new CountDownTimer(timeUntilNextDeadline, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        mTimeLeftTextView.setText(Utility.formatTimeLeft(mContext, millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        mTimeLeftTextView.setText(mContext.getString(R.string.time_up));
                        restartDeadlinesLoader();
                        Log.d(LOG_TAG, "Countdown finished");
                    }
                }.start();

                mLoaderManager.restartLoader(NEXT_DEADLINE_CURSOR_LOADER_ID, null, this);
            } else {
                if (null != sDeadlinesCountdownTimer) {
                    sDeadlinesCountdownTimer.cancel();
                }

                mTimeLeftTextView.setText(mContext.getString(R.string.no_deadline_countdown));
                mRecyclerView.setVisibility(View.GONE);
                mNoItemTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void restartDeadlinesLoader() {
        Log.d(LOG_TAG, "Waiting a few seconds before restarting loader");

        if (null != sDeadlineTimeUpDelayCountDownTimer) {
            sDeadlineTimeUpDelayCountDownTimer.cancel();
        }

        sDeadlineTimeUpDelayCountDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mLoaderManager.restartLoader(TASKS_CURSOR_LOADER_ID, null, DeadlinesCard.this);
            }
        }.start();
    }
}