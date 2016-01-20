package finalproject.productivityup.ui.deadlines;

import android.database.Cursor;
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

import finalproject.productivityup.R;
import finalproject.productivityup.adapter.DeadlineDaysCursorAdapter;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.ProductivityProvider;

/**
 * Displays deadline tasks in a list.
 */
public class DeadlinesActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int TASK_CURSOR_LOADER_START_ID = 1;
    private static final int DATE_CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DeadlineDaysCursorAdapter mDeadlineDaysCursorAdapter;
    private boolean mWillAutoScroll = true;
    private RecyclerView mRecyclerView;
    private int mRecentDeadlinePosition;
    private long mRecentDeadlineValue;
    private boolean didItemRangeChange = false;
    private boolean hasScrolled = false;

    public DeadlinesActivityFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ProductivityProvider.DeadlineDays.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Get most recent deadline and pass it to the tasks adapter.
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long todayInSeconds = today.getTimeInMillis() / 1000;

        mRecentDeadlinePosition = -1;
        while (data.moveToNext()) {
            mRecentDeadlinePosition = data.getPosition();
            if (data.getLong(data.getColumnIndex(DeadlineDaysColumns.DATE)) >= todayInSeconds) {
                mRecentDeadlineValue = data.getLong(data.getColumnIndex(DeadlineDaysColumns.DATE));
                mRecentDeadlinePosition = data.getPosition();
                break;
            }
        }

        mDeadlineDaysCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDeadlineDaysCursorAdapter.swapCursor(null);
    }

    public void scrollToDate(long unixDate) {
        if (mWillAutoScroll && mRecentDeadlineValue >= unixDate && !hasScrolled) {
            mWillAutoScroll = false;

            new CountDownTimer(100, 100) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Log.d(LOG_TAG, "Scrolling to position: " + mRecentDeadlinePosition);
                    mRecyclerView.scrollToPosition(mRecentDeadlinePosition);
                }
            }.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        getLoaderManager().restartLoader(DATE_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deadlines, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.deadlines_card_recycler_view);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        mDeadlineDaysCursorAdapter = new DeadlineDaysCursorAdapter(getActivity(), null, getLoaderManager());
        mRecyclerView.setAdapter(mDeadlineDaysCursorAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(LOG_TAG, "Recycler view scrolled: " + dy);

                if (dy == 0 && !didItemRangeChange) {
                    didItemRangeChange = true;
                } else if (dy == 0 && !hasScrolled) {
                    hasScrolled = true;
                } else if (dy != 0) {
                    hasScrolled = true;
                } else {
                    new CountDownTimer(100, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            Log.d(LOG_TAG, "Scrolling to position: " + mRecentDeadlinePosition);
                            mRecyclerView.scrollToPosition(mRecentDeadlinePosition);
                        }
                    }.start();
                }
            }
        });

        return rootView;
    }
}
