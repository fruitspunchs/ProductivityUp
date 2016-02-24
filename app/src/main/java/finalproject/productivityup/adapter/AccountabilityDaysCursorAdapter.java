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
 * Adapter for accountability cards and loads accountability card hours.
 */
public class AccountabilityDaysCursorAdapter extends CursorRecyclerViewAdapter<AccountabilityDaysCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int sHoursCursorLoaderId;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final LoaderManager mLoaderManager;
    private final String UNIX_DATE_KEY = "UNIX_DATE_KEY";
    private Context mContext;
    private List<AccountabilityHoursCursorAdapter> mCursorAdapterArrayList = new ArrayList<>();
    private boolean mGetNextImmediateDay = true;
    private long mNextImmediateDay = -1;
    //private DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder mViewHolder;

    private SharedPreferences mSharedPreferences;

    public AccountabilityDaysCursorAdapter(Context context, Cursor cursor, LoaderManager loaderManager) {
        super(context, cursor);
        mContext = context;
        mLoaderManager = loaderManager;
        sHoursCursorLoaderId = AccountabilityActivityFragment.HOURS_CURSOR_LOADER_START_ID;
        //mViewHolder = new DeadlineTasksCursorAdapter.DeadlineTasksLastSelectedItemViewHolder();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        //mViewHolder.mLastSelectedItem = mSharedPreferences.getLong(DeadlineTasksCursorAdapter.DEADLINES_LAST_SELECTED_ITEM_KEY, -1);
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
        viewHolder.mHoursRecyclerView.setAdapter(viewHolder.mCursorAdapter);

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
        mCursorAdapterArrayList.add(vh.mCursorAdapter);
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

        if (mCursorAdapterArrayList.size() >= loader.getId()) {
            if (mCursorAdapterArrayList.get(loader.getId() - 1) != null) {

                int SECONDS_IN_ONE_POINT_FIVE_HOUR = 5400;
                int SECONDS_IN_HALF_HOUR = 1800;
                int SECONDS_IN_TWO_HOURS = 7200;

                // TODO: 2/22/2016 collect tasks for each time slot

                //iterate dates
                //add date block to cursor

                //Create cursor to store time slots
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{AccountabilityHoursCursorAdapter.AccountabilityHoursColumns._ID,
                        AccountabilityHoursCursorAdapter.AccountabilityHoursColumns.START});

                //get first date
                //round to nearest 30 minute
                //add to cursor
                //next threshold = first date + 7200

                //get next date
                //if date >= next threshold
                //add to cursor
                //next threshold += 7200


                boolean firstItem = true;
                long threshold = 0;
                int id = 0;

                while (data.moveToNext()) {
                    long time = data.getLong(data.getColumnIndex(AccountabilityTasksColumns.TIME));

                    if (firstItem) {
                        firstItem = false;

                        long round = time % SECONDS_IN_HALF_HOUR;
                        if (round >= SECONDS_IN_HALF_HOUR / 2) {
                            threshold = time + round;
                        } else {
                            threshold = time - round;
                        }

                        matrixCursor.addRow(new Object[]{id++, threshold});
                        threshold += SECONDS_IN_TWO_HOURS;
                    } else {


                        if (time >= threshold) {
                            while (time >= threshold) {
                                threshold += SECONDS_IN_TWO_HOURS;
                            }

                            matrixCursor.addRow(new Object[]{id++, threshold - SECONDS_IN_TWO_HOURS});
                        }
                    }
                }

                mCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(matrixCursor);


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
        mCursorAdapterArrayList.get(loader.getId() - 1).swapCursor(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public RecyclerView mHoursRecyclerView;
        public View mView;
        public int mId;
        public long mUnixDate;
        public AccountabilityHoursCursorAdapter mCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateTextView = (TextView) view.findViewById(R.id.date_text_view);
            mHoursRecyclerView = (RecyclerView) view.findViewById(R.id.hours_recycler_view);
            mCursorAdapter = new AccountabilityHoursCursorAdapter(mContext, null, mLoaderManager);
            mId = sHoursCursorLoaderId++;
        }
    }
}
