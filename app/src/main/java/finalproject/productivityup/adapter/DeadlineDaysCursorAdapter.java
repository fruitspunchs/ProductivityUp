package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import finalproject.productivityup.DeadlinesActivity;
import finalproject.productivityup.DeadlinesActivityFragment;
import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.LinearLayoutManager;
import finalproject.productivityup.libs.Utility;

/**
 * Created by User on 12/17/2015.
 */
public class DeadlineDaysCursorAdapter extends CursorRecyclerViewAdapter<DeadlineDaysCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sTaskCursorLoaderId = DeadlinesActivityFragment.TASK_CURSOR_LOADER_START_ID;
    private final String LOG_TAG = this.getClass().getSimpleName();
    ViewHolder mVh;
    private Context mContext;
    private long mUnixDate;
    private List<DeadlineTasksCursorAdapter> mDeadlineTasksCursorAdapterArrayList = new ArrayList<>();
    private boolean mGetNextDeadline = true;
    private long mNextDeadline = -1;

    public DeadlineDaysCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        sTaskCursorLoaderId = DeadlinesActivityFragment.TASK_CURSOR_LOADER_START_ID;
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        mUnixDate = cursor.getLong(cursor.getColumnIndex(DeadlineDaysColumns.DATE));
        viewHolder.mDateTextView.setText(Utility.formatDate(mUnixDate));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        Log.d(LOG_TAG, "Today is: " + today.getTimeInMillis() / 1000);

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

        viewHolder.mTasksRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mDeadlineTasksCursorAdapter);
        Log.d(LOG_TAG, "Task cursor adapter items: " + viewHolder.mDeadlineTasksCursorAdapter.getItemCount());
        ((DeadlinesActivity) mContext).restartTaskCursorLoader(viewHolder.mId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        Log.d(LOG_TAG, "Creating viewholder. Id: " + mVh.mId);
        mDeadlineTasksCursorAdapterArrayList.add(mVh.mDeadlineTasksCursorAdapter);
        return vh;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "Creating loader. Id: " + id);
        String[] selectionArgs = {""};
        selectionArgs[0] = String.valueOf(mUnixDate);
        Log.d(LOG_TAG, "Date value: " + selectionArgs[0]);
        return new CursorLoader(mContext, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                null,
                DeadlineTasksColumns.DATE + " = ?",
                selectionArgs,
                DeadlineTasksColumns.TIME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Loader finished. Id: " + loader.getId());
        Log.d(LOG_TAG, "Cursor items: " + data.getCount());
        if (mVh != null) {
            if (mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1) != null) {
                mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(data);
                Log.d(LOG_TAG, "Swapping adapter " + mVh.mId + " with loader " + loader.getId());

            }
        }
    }

    public void restartAllLoaders() {
        for (DeadlineTasksCursorAdapter i : mDeadlineTasksCursorAdapterArrayList) {
            i.swapCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        restartAllLoaders();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public RecyclerView mTasksRecyclerView;
        public DeadlineTasksCursorAdapter mDeadlineTasksCursorAdapter;
        public int mId;

        public ViewHolder(View view) {
            super(view);
            mDateTextView = (TextView) view.findViewById(R.id.deadlines_card_date_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.deadlines_card_tasks_recycler_view);
            mDeadlineTasksCursorAdapter = new DeadlineTasksCursorAdapter(null, null);
            mId = sTaskCursorLoaderId++;
        }
    }
}
