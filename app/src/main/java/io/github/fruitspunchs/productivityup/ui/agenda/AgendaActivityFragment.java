/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.agenda;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.adapter.AgendaDaysCursorAdapter;
import io.github.fruitspunchs.productivityup.data.AgendaDaysColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class AgendaActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int TASK_CURSOR_LOADER_START_ID = 1;
    public static final int RESULT_CANCEL = 1;
    public static final int RESULT_ADD = 2;
    public static final int RESULT_EDIT = 3;
    public static final String ACTION_SCROLL_TO_NEAREST_DAY = "ACTION_SCROLL_TO_NEAREST_DAY";
    public static final String ACTION_NONE = "ACTION_NONE";
    public static final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    private static final int DATE_CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private AgendaDaysCursorAdapter mAgendaDaysCursorAdapter;
    private RecyclerView mRecyclerView;
    private int mNearestDayPosition;
    private int mResultItemPosition;
    private int mResult = RESULT_CANCEL;
    private long mResultUnixDate;
    private String mAction = ACTION_NONE;

    public AgendaActivityFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ProductivityProvider.AgendaDays.CONTENT_URI,
                null,
                null,
                null,
                AgendaDaysColumns.DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (mAction.equals(ACTION_SCROLL_TO_NEAREST_DAY)) {
            //Get most recent deadline
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            long todayInSeconds = today.getTimeInMillis() / 1000;

            mNearestDayPosition = -1;
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                mNearestDayPosition = data.getPosition();
                if (data.getLong(data.getColumnIndex(AgendaDaysColumns.DATE)) <= todayInSeconds) {
                    break;
                }
            }
        }

        //Add an empty item at the end so we can scroll the last item over the fab
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{AgendaDaysColumns._ID, AgendaDaysColumns.DATE});
        matrixCursor.addRow(new Object[]{-1, -1});

        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{data, matrixCursor});
        Log.d(LOG_TAG, "Merge cursor items: " + mergeCursor.getCount());

        mAgendaDaysCursorAdapter.swapCursor(mergeCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAgendaDaysCursorAdapter.swapCursor(null);
    }


    public void onViewAttachedToWindow(long unixDate) {
        if (mAction.equals(ACTION_SCROLL_TO_NEAREST_DAY)) {
            mAction = ACTION_NONE;
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                intent.setAction(ACTION_NONE);
            }

            new CountDownTimer(100, 100) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Log.d(LOG_TAG, "Scrolling to position: " + mNearestDayPosition);
                    mRecyclerView.scrollToPosition(mNearestDayPosition);
                }
            }.start();

        } else if (mResult == RESULT_ADD || mResult == RESULT_EDIT) {
            mResult = RESULT_CANCEL;

            Cursor data = mAgendaDaysCursorAdapter.getCursor();
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                if (data.getLong(data.getColumnIndex(AgendaDaysColumns.DATE)) == mResultUnixDate) {
                    mResultItemPosition = data.getPosition();
                }
            }

            new CountDownTimer(100, 100) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Log.d(LOG_TAG, "Scrolling to added item: " + mResultItemPosition);
                    mRecyclerView.scrollToPosition(mResultItemPosition);
                }
            }.start();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DATE_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agenda, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.getAction() != null) {
                mAction = intent.getAction();
            }
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(this.getResources().getInteger(R.integer.grid_rows), StaggeredGridLayoutManager.VERTICAL));
        mAgendaDaysCursorAdapter = new AgendaDaysCursorAdapter(getActivity(), null, getLoaderManager());
        mRecyclerView.setAdapter(mAgendaDaysCursorAdapter);

        return rootView;
    }

    public void onResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onResult");
        super.onActivityResult(requestCode, resultCode, data);
        mResult = resultCode;

        if (data != null) {
            mResultUnixDate = data.getLongExtra(UNIX_DATE_KEY, 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getView() != null) {
            int horizontalPadding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
            int topPadding = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
            getView().setPadding(horizontalPadding, topPadding, horizontalPadding, 0);
        }
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(this.getResources().getInteger(R.integer.grid_rows), StaggeredGridLayoutManager.VERTICAL));
    }
}
