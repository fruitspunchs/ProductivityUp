package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import finalproject.productivityup.R;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.agenda.AgendaActivity;
import finalproject.productivityup.ui.agenda.AgendaActivityFragment;

/**
 * Created by User on 1/12/2016.
 */
public class AgendaDaysCursorAdapter extends CursorRecyclerViewAdapter<AgendaDaysCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sTaskCursorLoaderId = AgendaActivityFragment.TASK_CURSOR_LOADER_START_ID;
    private final String LOG_TAG = this.getClass().getSimpleName();
    ViewHolder mVh;
    private Context mContext;
    private long mUnixDate;
    private List<AgendaTasksCursorAdapter> mAgendaTasksCursorAdapterArrayList = new ArrayList<>();
    private boolean mGetNextDeadline = true;
    private long mNextDeadline = -1;
    private int mScrollToPosition = -1;

    public AgendaDaysCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        sTaskCursorLoaderId = AgendaActivityFragment.TASK_CURSOR_LOADER_START_ID;
    }

    @Override
    public void onBindViewHolder(AgendaDaysCursorAdapter.ViewHolder viewHolder, Cursor cursor) {
        mUnixDate = cursor.getLong(cursor.getColumnIndex(DeadlineDaysColumns.DATE));
        viewHolder.mAgendaTasksCursorAdapter.setContext(mContext);
        viewHolder.mDateTextView.setText(Utility.formatDate(mUnixDate));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        //// TODO: 1/12/2016 if no today or tomorrow add blank card, only today is red text
        //Set text colors for easier reading
        if (mUnixDate == mNextDeadline) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else if (mUnixDate < today.getTimeInMillis() / 1000) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryText));
        } else if (mGetNextDeadline) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            mGetNextDeadline = false;
            mNextDeadline = mUnixDate;
        } else {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
        }

        viewHolder.mTasksRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mAgendaTasksCursorAdapter);
        Log.d(LOG_TAG, "Task cursor adapter items: " + viewHolder.mAgendaTasksCursorAdapter.getItemCount());
        ((AppCompatActivity) mContext).getSupportLoaderManager().restartLoader(viewHolder.mId, null, this);
    }

    @Override
    public AgendaDaysCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agenda_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        Log.d(LOG_TAG, "Creating viewholder. Id: " + mVh.mId);
        mAgendaTasksCursorAdapterArrayList.add(mVh.mAgendaTasksCursorAdapter);
        return vh;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "Creating loader. Id: " + id);
        String[] selectionArgs = {""};
        selectionArgs[0] = String.valueOf(mUnixDate);
        Log.d(LOG_TAG, "Date value: " + selectionArgs[0]);
        return new CursorLoader(mContext, ProductivityProvider.AgendaTasks.CONTENT_URI,
                null,
                AgendaTasksColumns.DATE + " = ?",
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Loader finished. Id: " + loader.getId());
        Log.d(LOG_TAG, "Cursor items: " + data.getCount());
        if (mVh != null) {
            if (mAgendaTasksCursorAdapterArrayList.get(loader.getId() - 1) != null) {
                mAgendaTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(data);
                Log.d(LOG_TAG, "Swapping adapter " + mVh.mId + " with loader " + loader.getId());

                //Scroll to most recent deadline after recycler views are populated.
                if (mScrollToPosition != -1) {
                    ((AgendaActivity) mContext).scrollToPosition(mScrollToPosition);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        restartAllLoaders();
    }

    public void setScrollToPosition(int position) {
        mScrollToPosition = position;
    }


    public void restartAllLoaders() {
        for (AgendaTasksCursorAdapter i : mAgendaTasksCursorAdapterArrayList) {
            i.swapCursor(null);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public RecyclerView mTasksRecyclerView;
        public View mView;
        public int mId;
        AgendaTasksCursorAdapter mAgendaTasksCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateTextView = (TextView) view.findViewById(R.id.date_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tasks_recycler_view);
            mAgendaTasksCursorAdapter = new AgendaTasksCursorAdapter(mContext, null);
            mId = sTaskCursorLoaderId++;
        }
    }
}
