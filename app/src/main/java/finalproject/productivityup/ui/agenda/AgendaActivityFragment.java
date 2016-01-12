package finalproject.productivityup.ui.agenda;

import android.database.Cursor;
import android.os.Bundle;
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
import finalproject.productivityup.adapter.AgendaDaysCursorAdapter;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.ProductivityProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class AgendaActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TASK_CURSOR_LOADER_START_ID = 1;
    private static final int DATE_CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private boolean mWillAutoScroll = true;
    private AgendaDaysCursorAdapter mAgendaDaysCursorAdapter;
    private RecyclerView mRecyclerView;

    public AgendaActivityFragment() {
    }

    //TODO: Fix scroll offset
    public void scrollToPosition(int position) {
        if (mWillAutoScroll) {
            mWillAutoScroll = false;
            Log.d(LOG_TAG, "Scrolling to position: " + position);
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ProductivityProvider.AgendaDays.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAgendaDaysCursorAdapter.swapCursor(data);

        //Get most recent deadline and pass it to the tasks adapter.
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long todayInSeconds = today.getTimeInMillis() / 1000;

        int recentDeadlinePosition = -1;

        while (data.moveToNext()) {
            recentDeadlinePosition = data.getPosition();
            if (data.getLong(data.getColumnIndex(DeadlineDaysColumns.DATE)) >= todayInSeconds) {
                recentDeadlinePosition = data.getPosition();
                break;
            }
        }

        mAgendaDaysCursorAdapter.setScrollToPosition(recentDeadlinePosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAgendaDaysCursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DATE_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DATE_CURSOR_LOADER_ID, null, this);
        if (mAgendaDaysCursorAdapter != null) {
            mAgendaDaysCursorAdapter.restartAllLoaders();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deadlines, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.deadlines_card_recycler_view);

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        mAgendaDaysCursorAdapter = new AgendaDaysCursorAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAgendaDaysCursorAdapter);
        return rootView;
    }
}
