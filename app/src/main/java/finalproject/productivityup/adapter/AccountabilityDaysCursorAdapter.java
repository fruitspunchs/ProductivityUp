package finalproject.productivityup.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import finalproject.productivityup.data.AccountabilityTasksColumns;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.accountability.AccountabilityActivityFragment;

// FIXME: 1/23/2016 onLoadFinished doesn't get called sometimes because the loader is restarted too quickly

/**
 * Adapter for accountability cards and loads accountability items.
 */
public class AccountabilityDaysCursorAdapter extends CursorRecyclerViewAdapter<AccountabilityDaysCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sHoursCursorLoaderId;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final LoaderManager mLoaderManager;
    private final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    private Context mContext;
    private List<AccountabilityHoursCursorAdapter> mHoursCursorAdapterList = new ArrayList<>();
    private boolean mGetNextImmediateDay = true;
    private long mNextImmediateDay = -1;

    private AccountabilityTasksCursorAdapter.LastSelectedItemViewHolder mViewHolder;
    private SharedPreferences mSharedPreferences;

    public AccountabilityDaysCursorAdapter(Context context, Cursor cursor, LoaderManager loaderManager) {
        super(context, cursor);
        mContext = context;
        mLoaderManager = loaderManager;
        sHoursCursorLoaderId = AccountabilityActivityFragment.HOURS_CURSOR_LOADER_START_ID;

        mViewHolder = new AccountabilityTasksCursorAdapter.LastSelectedItemViewHolder();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mViewHolder.mLastSelectedItem = mSharedPreferences.getLong(AccountabilityTasksCursorAdapter.ACCOUNTABILITY_LAST_SELECTED_ITEM_KEY, -1);
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
        if (mUnixDate == mNextImmediateDay) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else if (mUnixDate < today.getTimeInMillis() / 1000) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorSecondaryText));
        } else if (mGetNextImmediateDay) {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            mGetNextImmediateDay = false;
            mNextImmediateDay = mUnixDate;
        } else {
            viewHolder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
        }

        Log.d(LOG_TAG, "Binding ViewHolder. Id: " + viewHolder.mId);
        viewHolder.mHoursRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        viewHolder.mHoursRecyclerView.setAdapter(viewHolder.mHoursCursorAdapter);

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
                .inflate(R.layout.item_accountability_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        Log.d(LOG_TAG, "Creating ViewHolder. Id: " + vh.mId);
        mHoursCursorAdapterList.add(vh.mHoursCursorAdapter);
        return vh;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "Creating loader. Id: " + id);
        String[] selectionArgs = {""};
        selectionArgs[0] = String.valueOf(args.getLong(UNIX_DATE_KEY));
        return new CursorLoader(mContext, ProductivityProvider.AccountabilityChartTasks.CONTENT_URI,
                null,
                AccountabilityTasksColumns.DATE + " = ?",
                selectionArgs,
                AccountabilityTasksColumns.TIME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Loader finished. Id: " + loader.getId());
        Log.d(LOG_TAG, "Cursor items: " + data.getCount());

        if (mHoursCursorAdapterList.size() >= loader.getId()) {
            if (mHoursCursorAdapterList.get(loader.getId() - 1) != null) {

                int SECONDS_IN_HALF_HOUR = 1800;
                int SECONDS_IN_TWO_HOURS = 7200;

                //Create cursor to store time slots
                MatrixCursor timeSlotCursor = new MatrixCursor(new String[]{AccountabilityHoursCursorAdapter.AccountabilityHoursColumns._ID,
                        AccountabilityHoursCursorAdapter.AccountabilityHoursColumns.START});

                List<MatrixCursor> taskCursorList = new ArrayList<>();

                boolean firstItem = true;
                long threshold = 0;
                int timeSlotId = 0;
                int taskCursorListIndex = 0;

                while (data.moveToNext()) {
                    long timeSpanStart = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));

                    if (firstItem) {
                        firstItem = false;

                        long round = timeSpanStart % SECONDS_IN_HALF_HOUR;
                        if (round >= SECONDS_IN_HALF_HOUR / 2) {
                            threshold = timeSpanStart + SECONDS_IN_HALF_HOUR - round;
                        } else {
                            threshold = timeSpanStart - round;
                        }

                        timeSlotCursor.addRow(new Object[]{timeSlotId++, threshold});
                        threshold += SECONDS_IN_TWO_HOURS;

                        MatrixCursor tmpMatrixCursor = new MatrixCursor(new String[]{AccountabilityTasksColumns._ID, AccountabilityTasksColumns.DATE, AccountabilityTasksColumns.TIME, AccountabilityTasksColumns.TASK});
                        taskCursorList.add(tmpMatrixCursor);
                        long id = data.getLong(data.getColumnIndex(AccountabilityTasksColumns._ID));
                        long date = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.DATE));
                        long time = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));
                        String task = data.getString(data.getColumnIndex(AccountabilityTasksColumns.TASK));

                        taskCursorList.get(taskCursorListIndex).addRow(new Object[]{id, date, time, task});

                    } else {
                        if (timeSpanStart >= threshold) {
                            while (timeSpanStart >= threshold) {
                                threshold += SECONDS_IN_TWO_HOURS;
                            }

                            timeSlotCursor.addRow(new Object[]{timeSlotId++, threshold - SECONDS_IN_TWO_HOURS});

                            taskCursorListIndex++;
                            MatrixCursor tmpMatrixCursor = new MatrixCursor(new String[]{AccountabilityTasksColumns._ID, AccountabilityTasksColumns.DATE, AccountabilityTasksColumns.TIME, AccountabilityTasksColumns.TASK});
                            taskCursorList.add(tmpMatrixCursor);
                        }

                        long id = data.getLong(data.getColumnIndex(AccountabilityTasksColumns._ID));
                        long date = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.DATE));
                        long time = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));
                        String task = data.getString(data.getColumnIndex(AccountabilityTasksColumns.TASK));

                        taskCursorList.get(taskCursorListIndex).addRow(new Object[]{id, date, time, task});
                    }

                }

                mHoursCursorAdapterList.get(loader.getId() - 1).swapCursor(timeSlotCursor);
                mHoursCursorAdapterList.get(loader.getId() - 1).setTaskCursorList(taskCursorList);
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
        mHoursCursorAdapterList.get(loader.getId() - 1).swapCursor(null);
        mHoursCursorAdapterList.get(loader.getId() - 1).setTaskCursorList(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public RecyclerView mHoursRecyclerView;
        public View mView;
        public int mId;
        public long mUnixDate;
        public AccountabilityHoursCursorAdapter mHoursCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateTextView = (TextView) view.findViewById(R.id.date_text_view);
            mHoursRecyclerView = (RecyclerView) view.findViewById(R.id.hours_recycler_view);
            mHoursCursorAdapter = new AccountabilityHoursCursorAdapter(mContext, null, mViewHolder, mSharedPreferences);
            mId = sHoursCursorLoaderId++;
        }
    }
}
