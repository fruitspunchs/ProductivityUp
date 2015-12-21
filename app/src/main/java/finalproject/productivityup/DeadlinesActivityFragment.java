package finalproject.productivityup;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import finalproject.productivityup.adapter.DeadlineDaysCursorAdapter;
import finalproject.productivityup.data.ProductivityProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class DeadlinesActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TASK_CURSOR_LOADER_START_ID = 1;
    private static final int DATE_CURSOR_LOADER_ID = 0;
    private DeadlineDaysCursorAdapter mDeadlineDaysCursorAdapter;

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
        mDeadlineDaysCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDeadlineDaysCursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DATE_CURSOR_LOADER_ID, null, this);
    }

    public void restartTaskCursorLoader(int id) {
        getLoaderManager().restartLoader(id, null, mDeadlineDaysCursorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(DATE_CURSOR_LOADER_ID, null, this);
        if (mDeadlineDaysCursorAdapter != null) {
            mDeadlineDaysCursorAdapter.restartAllLoaders();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deadlines, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.deadlines_card_recycler_view);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        mDeadlineDaysCursorAdapter = new DeadlineDaysCursorAdapter(getActivity(), null);
        recyclerView.setAdapter(mDeadlineDaysCursorAdapter);
        return rootView;
    }
}
