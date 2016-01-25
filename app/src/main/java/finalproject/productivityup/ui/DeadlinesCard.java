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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import finalproject.productivityup.R;
import finalproject.productivityup.adapter.OverviewDeadlinesCursorAdapter;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;

public class DeadlinesCard implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DEADLINE_TASKS_CURSOR_LOADER_ID = 0;
    private static final int NEXT_DEADLINE_CURSOR_LOADER_ID = 1;
    private final String LOG_TAG = getClass().getSimpleName();
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private final RecyclerView mRecyclerView;
    private final TextView mNoItemTextView;
    private final LinearLayout mLayoutContainer;
    private final TextView mTimeLeftTextView;
    private boolean mHasItems = true;
    private boolean mIsShowingCardTitles = true;
    private OverviewDeadlinesCursorAdapter mCursorAdapter;
    private long mNextDeadlineUnixTime = -1;
    private CountDownTimer mDeadlinesCountdownTimer;
    private CountDownTimer mDeadlineTimeUpDelayCountDownTimer;

    public DeadlinesCard(Context context, LoaderManager loaderManager, RecyclerView recyclerView, TextView timeLeftTextView, TextView noItemTextView, LinearLayout layoutContainer) {
        mContext = context;
        mLoaderManager = loaderManager;
        mRecyclerView = recyclerView;
        mTimeLeftTextView = timeLeftTextView;
        mNoItemTextView = noItemTextView;
        mLayoutContainer = layoutContainer;
    }

    public void onCreate() {
        mLoaderManager.initLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        mCursorAdapter = new OverviewDeadlinesCursorAdapter(mContext, null);
        mRecyclerView.setAdapter(mCursorAdapter);
    }

    public void onStart() {
        mLoaderManager.restartLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, this);
        adjustDeadlinesLayout(mIsShowingCardTitles);
    }

    public void toggleCardTitles(boolean isShowingCardTitles) {
        mIsShowingCardTitles = isShowingCardTitles;
        adjustDeadlinesLayout(isShowingCardTitles);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == DEADLINE_TASKS_CURSOR_LOADER_ID) {
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
        } else if (loader.getId() == DEADLINE_TASKS_CURSOR_LOADER_ID) {
            if (data.moveToNext()) {

                mHasItems = true;

                mTimeLeftTextView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoItemTextView.setVisibility(View.GONE);

                mNextDeadlineUnixTime = data.getLong(data.getColumnIndex(DeadlineTasksColumns.TIME));

                long timeUntilNextDeadline = mNextDeadlineUnixTime * 1000 - System.currentTimeMillis();

                if (null != mDeadlinesCountdownTimer) {
                    mDeadlinesCountdownTimer.cancel();
                }

                mDeadlinesCountdownTimer = new CountDownTimer(timeUntilNextDeadline, 1000) {

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

                adjustDeadlinesLayout(mIsShowingCardTitles);

            } else {
                mTimeLeftTextView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mNoItemTextView.setVisibility(View.VISIBLE);

                mHasItems = false;

                adjustDeadlinesLayout(mIsShowingCardTitles);

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void adjustDeadlinesLayout(boolean isShowingCardTitles) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        if (isShowingCardTitles || mHasItems) {
            layoutParams.setMargins(0, 0, 0, mContext.getResources().getDimensionPixelSize(R.dimen.card_padding_bottom));
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        mLayoutContainer.setLayoutParams(layoutParams);
    }

    private void restartDeadlinesLoader() {
        Log.d(LOG_TAG, "Waiting a few seconds before restarting loader");

        if (null != mDeadlineTimeUpDelayCountDownTimer) {
            mDeadlineTimeUpDelayCountDownTimer.cancel();
        }

        final DeadlinesCard thisClass = this;

        mDeadlineTimeUpDelayCountDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mLoaderManager.restartLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, thisClass);
            }
        }.start();
    }
}