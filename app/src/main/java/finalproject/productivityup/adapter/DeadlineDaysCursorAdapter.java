package finalproject.productivityup.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.deadlines.DeadlinesActivityFragment;

// FIXME: 1/23/2016 onLoadFinished doesn't get called sometimes because the loader is restarted too quickly

/**
 * Adapter for deadline cards and loads deadline card items.
 */
public class DeadlineDaysCursorAdapter extends CursorRecyclerViewAdapter<DeadlineDaysCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sTaskCursorLoaderId;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final LoaderManager mLoaderManager;
    private final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    private Context mContext;
    private List<DeadlineTasksCursorAdapter> mDeadlineTasksCursorAdapterArrayList = new ArrayList<>();
    private boolean mGetNextDeadline = true;
    private long mNextDeadline = -1;
    private DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder mDeadlineTasksLastSelectedItemViewHolder;
    private SharedPreferences mSharedPreferences;

    public DeadlineDaysCursorAdapter(Context context, Cursor cursor, LoaderManager loaderManager) {
        super(context, cursor);
        mContext = context;
        mLoaderManager = loaderManager;
        sTaskCursorLoaderId = DeadlinesActivityFragment.TASK_CURSOR_LOADER_START_ID;
        mDeadlineTasksLastSelectedItemViewHolder = new DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDeadlineTasksLastSelectedItemViewHolder.mLastSelectedItem = mSharedPreferences.getLong(DeadlineTasksCursorAdapter.DEADLINES_LAST_SELECTED_ITEM_KEY, -1);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        long mUnixDate = cursor.getLong(cursor.getColumnIndex(DeadlineDaysColumns.DATE));
        viewHolder.mDateTextView.setText(Utility.formatDate(mUnixDate));
        viewHolder.mUnixDate = mUnixDate;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

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

        Log.d(LOG_TAG, "Binding ViewHolder. Id: " + viewHolder.mId);
        viewHolder.mTasksRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mDeadlineTasksCursorAdapter);

        //Set placeholder view at the end of the list as invisible
        if (viewHolder.mUnixDate == -1) {
            viewHolder.mView.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.mView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        Log.d(LOG_TAG, "Creating ViewHolder. Id: " + vh.mId);
        mDeadlineTasksCursorAdapterArrayList.add(vh.mDeadlineTasksCursorAdapter);
        return vh;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "Creating loader. Id: " + id);
        String[] selectionArgs = {""};
        selectionArgs[0] = String.valueOf(args.getLong(UNIX_DATE_KEY));
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

        if (mDeadlineTasksCursorAdapterArrayList.size() >= loader.getId()) {
            if (mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1) != null) {
                mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(data);
            }
        }

    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(LOG_TAG, "Restarting loader: " + holder.mId);
        Bundle args = new Bundle();
        args.putLong(UNIX_DATE_KEY, holder.mUnixDate);
        mLoaderManager.restartLoader(holder.mId, args, this);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public RecyclerView mTasksRecyclerView;
        public View mView;
        public int mId;
        public long mUnixDate;
        public DeadlineTasksCursorAdapter mDeadlineTasksCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateTextView = (TextView) view.findViewById(R.id.deadlines_card_date_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.deadlines_card_tasks_recycler_view);
            mDeadlineTasksCursorAdapter = new DeadlineTasksCursorAdapter(mContext, null, mDeadlineTasksLastSelectedItemViewHolder, mSharedPreferences);
            mId = sTaskCursorLoaderId++;
        }
    }
}
