package finalproject.productivityup.ui;

import android.database.Cursor;
import android.os.CountDownTimer;
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

public class DeadlinesCard {

    private final String LOG_TAG = getClass().getSimpleName();

    private final MainActivity mMainActivity;
    private final RecyclerView mDeadlinesTaskRecyclerView;
    private final TextView mDeadlinesTimeLeftTextView;
    private final TextView mDeadlinesNoItemTextView;
    private final LinearLayout mDeadlinesCardContainer;
    private OverviewDeadlinesCursorAdapter mOverviewDeadlinesCursorAdapter;
    private boolean mHasDeadlines = true;
    private long mNextDeadlineUnixTime = -1;
    private CountDownTimer mDeadlinesCountdownTimer;
    private CountDownTimer mDeadlineTimeUpDelayCountDownTimer;
    private boolean mIsShowingCardTitles;

    public DeadlinesCard(MainActivity mainActivity, RecyclerView deadlinesTaskRecyclerView, TextView deadlinesTimeLeftTextView, TextView deadlinesNoItemTextView, LinearLayout deadlinesCardContainer) {

        mMainActivity = mainActivity;
        mDeadlinesTaskRecyclerView = deadlinesTaskRecyclerView;
        mDeadlinesTimeLeftTextView = deadlinesTimeLeftTextView;
        mDeadlinesNoItemTextView = deadlinesNoItemTextView;
        mDeadlinesCardContainer = deadlinesCardContainer;
    }

    public void onCreate() {
        mMainActivity.getSupportLoaderManager().initLoader(MainActivity.DEADLINE_TASKS_CURSOR_LOADER_ID, null, mMainActivity);
        mDeadlinesTaskRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mMainActivity));
        mOverviewDeadlinesCursorAdapter = new OverviewDeadlinesCursorAdapter(mMainActivity, null);
        mDeadlinesTaskRecyclerView.setAdapter(mOverviewDeadlinesCursorAdapter);
    }

    public void onStart() {
        mMainActivity.getSupportLoaderManager().restartLoader(MainActivity.DEADLINE_TASKS_CURSOR_LOADER_ID, null, mMainActivity);
    }

    public void toggleCardTitles(boolean isShowingCardTitles) {
        mIsShowingCardTitles = isShowingCardTitles;
        adjustDeadlinesLayout(isShowingCardTitles);
    }

    public CursorLoader onCreateLoader(int id) {
        if (id == MainActivity.DEADLINE_TASKS_CURSOR_LOADER_ID) {
            Calendar currentTime = Calendar.getInstance();
            long currentTimeInSeconds = currentTime.getTimeInMillis() / 1000;
            String[] selectionArgs = {String.valueOf(currentTimeInSeconds)};

            return new CursorLoader(mMainActivity, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " > ?",
                    selectionArgs,
                    DeadlineTasksColumns.TIME + " ASC");
        } else if (id == MainActivity.NEXT_DEADLINE_CURSOR_LOADER_ID) {
            String[] selectionArgs = {String.valueOf(mNextDeadlineUnixTime)};

            return new CursorLoader(mMainActivity, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " = ?",
                    selectionArgs,
                    null);
        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == MainActivity.NEXT_DEADLINE_CURSOR_LOADER_ID) {
            mOverviewDeadlinesCursorAdapter.swapCursor(data);
        } else if (loader.getId() == MainActivity.DEADLINE_TASKS_CURSOR_LOADER_ID) {
            if (data.moveToNext()) {

                mHasDeadlines = true;

                mDeadlinesTimeLeftTextView.setVisibility(View.VISIBLE);
                mDeadlinesTaskRecyclerView.setVisibility(View.VISIBLE);
                mDeadlinesNoItemTextView.setVisibility(View.GONE);

                mNextDeadlineUnixTime = data.getLong(data.getColumnIndex(DeadlineTasksColumns.TIME));

                long timeUntilNextDeadline = mNextDeadlineUnixTime * 1000 - System.currentTimeMillis();

                if (null != mDeadlinesCountdownTimer) {
                    mDeadlinesCountdownTimer.cancel();
                }

                mDeadlinesCountdownTimer = new CountDownTimer(timeUntilNextDeadline, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        mDeadlinesTimeLeftTextView.setText(Utility.formatTimeLeft(mMainActivity, millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        mDeadlinesTimeLeftTextView.setText(mMainActivity.getString(R.string.time_up));
                        restartDeadlinesLoader();
                        Log.d(LOG_TAG, "Countdown finished");
                    }
                }.start();

                mMainActivity.getSupportLoaderManager().restartLoader(MainActivity.NEXT_DEADLINE_CURSOR_LOADER_ID, null, mMainActivity);

                adjustDeadlinesLayout(mIsShowingCardTitles);

            } else {
                mDeadlinesTimeLeftTextView.setVisibility(View.GONE);
                mDeadlinesTaskRecyclerView.setVisibility(View.GONE);
                mDeadlinesNoItemTextView.setVisibility(View.VISIBLE);

                mHasDeadlines = false;

                adjustDeadlinesLayout(mIsShowingCardTitles);

            }
        }
    }

    private void adjustDeadlinesLayout(boolean isShowingCardTitles) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        if (isShowingCardTitles || mHasDeadlines) {
            layoutParams.setMargins(0, 0, 0, mMainActivity.getResources().getDimensionPixelSize(R.dimen.card_padding_bottom));
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        mDeadlinesCardContainer.setLayoutParams(layoutParams);
    }

    private void restartDeadlinesLoader() {
        Log.d(LOG_TAG, "Waiting a few seconds before restarting loader");

        if (null != mDeadlineTimeUpDelayCountDownTimer) {
            mDeadlineTimeUpDelayCountDownTimer.cancel();
        }

        mDeadlineTimeUpDelayCountDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mMainActivity.getSupportLoaderManager().restartLoader(MainActivity.DEADLINE_TASKS_CURSOR_LOADER_ID, null, mMainActivity);
            }
        }.start();
    }

    public void onLoaderReset() {
        mOverviewDeadlinesCursorAdapter.swapCursor(null);
    }
}