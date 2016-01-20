package finalproject.productivityup.ui.deadlines;

import android.content.Intent;
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
    public static final String ACTION_SCROLL_TO_NEAREST_DEADLINE = "ACTION_SCROLL_TO_NEAREST_DEADLINE";
    public static final String ACTION_NONE = "ACTION_NONE";
    public static final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    public static final int RESULT_INVALID = 1;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_ADD = 3;
    public static final int RESULT_EDIT = 4;
    private static final int DATE_CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DeadlineDaysCursorAdapter mDeadlineDaysCursorAdapter;
    private boolean mWillAutoScroll = true;
    private RecyclerView mRecyclerView;
    private int mRecentDeadlinePosition;
    private long mRecentDeadlineValue;
    private int mResultItemPosition;
    private int result = RESULT_INVALID;
    private long resultUnixDate;

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

        //Get most recent deadline
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long todayInSeconds = today.getTimeInMillis() / 1000;

        mRecentDeadlinePosition = -1;
        data.moveToPosition(-1);
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

    public void onViewAttachedToWindow(long unixDate) {
        String action = ACTION_NONE;

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.getAction() != null) {
                action = intent.getAction();
            }
        }

        if (action.equals(ACTION_SCROLL_TO_NEAREST_DEADLINE)) {
            if (mWillAutoScroll && mRecentDeadlineValue >= unixDate) {
                mWillAutoScroll = false;

                if (intent != null) {
                    intent.setAction(ACTION_NONE);
                }

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
        } else if (result == RESULT_ADD || result == RESULT_EDIT) {
            result = RESULT_INVALID;

            Cursor data = mDeadlineDaysCursorAdapter.getCursor();
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                if (data.getLong(data.getColumnIndex(DeadlineDaysColumns.DATE)) == resultUnixDate) {
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

        return rootView;
    }

    public void onResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onResult");
        super.onActivityResult(requestCode, resultCode, data);
        result = resultCode;

        if (data != null) {
            resultUnixDate = data.getLongExtra(UNIX_DATE_KEY, 0);
        }
    }
}
