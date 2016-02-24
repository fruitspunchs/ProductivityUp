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

import java.util.Calendar;

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.ui.accountability.AccountabilityActivityFragment;

// FIXME: 1/23/2016 onLoadFinished doesn't get called sometimes because the loader is restarted too quickly
// TODO: 2/22/2016 check if and prevent if loader id overlap possible
// TODO: 2/22/2016 auto scroll to most immediate task item
// TODO: 2/22/2016 adjust loader logic to start id

/**
 * Adapter for deadline cards and loads deadline card items.
 */
public class AccountabilityHoursCursorAdapter extends CursorRecyclerViewAdapter<AccountabilityHoursCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sTaskCursorLoaderId;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final LoaderManager mLoaderManager;
    private final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    private Context mContext;
    //private List<DeadlineTasksCursorAdapter> mDeadlineTasksCursorAdapterArrayList = new ArrayList<>();
    //private DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder mViewHolder;

    private SharedPreferences mSharedPreferences;

    public AccountabilityHoursCursorAdapter(Context context, Cursor cursor, LoaderManager loaderManager) {
        super(context, cursor);
        mContext = context;
        mLoaderManager = loaderManager;
        sTaskCursorLoaderId = AccountabilityActivityFragment.TASK_CURSOR_LOADER_START_ID;
        //mViewHolder = new DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        //mViewHolder.mLastSelectedItem = mSharedPreferences.getLong(DeadlineTasksCursorAdapter.DEADLINES_LAST_SELECTED_ITEM_KEY, -1);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        long timeSpan = cursor.getLong(cursor.getColumnIndex(AccountabilityHoursColumns.START));
        viewHolder.mHoursTextView.setText(String.valueOf(timeSpan));

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Log.d(LOG_TAG, "Binding ViewHolder. Id: " + viewHolder.mId);
//        viewHolder.mTasksRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
//        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mDeadlineTasksCursorAdapter);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accountability_hours, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        Log.d(LOG_TAG, "Creating ViewHolder. Id: " + vh.mId);
        //mDeadlineTasksCursorAdapterArrayList.add(vh.mDeadlineTasksCursorAdapter);
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

//        if (mDeadlineTasksCursorAdapterArrayList.size() >= loader.getId()) {
//            if (mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1) != null) {
//                mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(data);
//            }
//        }

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
//        mDeadlineTasksCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(null);
    }

    public interface AccountabilityHoursColumns {
        String _ID = "_id";
        String START = "START";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mHoursTextView;
        public RecyclerView mTasksRecyclerView;
        public View mView;
        public int mId;
        public long mUnixDate;
//        public DeadlineTasksCursorAdapter mDeadlineTasksCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mHoursTextView = (TextView) view.findViewById(R.id.hours_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tasks_recycler_view);
//            mDeadlineTasksCursorAdapter = new DeadlineTasksCursorAdapter(mContext, null, mViewHolder, mSharedPreferences);
            mId = sTaskCursorLoaderId++;
        }
    }
}
