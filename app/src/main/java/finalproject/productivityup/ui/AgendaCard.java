package finalproject.productivityup.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import finalproject.productivityup.adapter.OverviewAgendaCursorAdapter;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;

/**
 * Created by User on 1/25/2016.
 */
public class AgendaCard implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int AGENDA_CURSOR_LOADER_ID = 2;
    private static CountDownTimer sNextDayCountdownTimer;
    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private final RecyclerView mRecyclerView;
    private final TextView mNoItemTextView;
    private OverviewAgendaCursorAdapter mCursorAdapter;

    public AgendaCard(Context context, LoaderManager loaderManager, RecyclerView recyclerView, TextView noItemTextView) {
        mContext = context;
        mLoaderManager = loaderManager;
        mRecyclerView = recyclerView;
        mNoItemTextView = noItemTextView;
    }

    public void onCreate() {
        mLoaderManager.initLoader(AGENDA_CURSOR_LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        mCursorAdapter = new OverviewAgendaCursorAdapter(mContext, null);
        mRecyclerView.setAdapter(mCursorAdapter);
    }

    public void onStart() {
        mLoaderManager.restartLoader(AGENDA_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == AGENDA_CURSOR_LOADER_ID) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            long currentTimeInSeconds = today.getTimeInMillis() / 1000;
            String[] selectionArgs = {String.valueOf(currentTimeInSeconds)};
            Log.d(LOG_TAG, "Loading date: " + selectionArgs[0]);

            return new CursorLoader(mContext, ProductivityProvider.AgendaTasks.CONTENT_URI,
                    null,
                    AgendaTasksColumns.DATE + " = ?",
                    selectionArgs,
                    AgendaTasksColumns.DATE + " ASC");
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == AGENDA_CURSOR_LOADER_ID) {
            if (data.moveToNext()) {
                Log.d(LOG_TAG, "Has items");
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoItemTextView.setVisibility(View.GONE);

                mCursorAdapter.swapCursor(data);
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

            final AgendaCard thisClass = this;

            sNextDayCountdownTimer = new CountDownTimer(millisUntilTomorrow, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mLoaderManager.restartLoader(AGENDA_CURSOR_LOADER_ID, null, thisClass);
                }
            }.start();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AGENDA_CURSOR_LOADER_ID) {
            mCursorAdapter.swapCursor(null);
        }
    }
}
