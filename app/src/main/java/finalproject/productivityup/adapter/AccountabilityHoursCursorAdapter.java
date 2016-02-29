package finalproject.productivityup.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;

// FIXME: 1/23/2016 onLoadFinished doesn't get called sometimes because the loader is restarted too quickly
// TODO: 2/22/2016 auto scroll to most immediate task item

/**
 * Adapter for deadline cards and loads deadline card items.
 */
public class AccountabilityHoursCursorAdapter extends CursorRecyclerViewAdapter<AccountabilityHoursCursorAdapter.ViewHolder> {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private final AccountabilityTasksCursorAdapter.LastSelectedItemViewHolder mViewHolder;
    private final SharedPreferences mSharedPreferences;
    private Context mContext;
    private List<MatrixCursor> mTaskCursorList;

    public AccountabilityHoursCursorAdapter(Context context, Cursor cursor, AccountabilityTasksCursorAdapter.LastSelectedItemViewHolder viewHolder, SharedPreferences sharedPreferences) {
        super(context, cursor);
        mContext = context;
        mViewHolder = viewHolder;
        mSharedPreferences = sharedPreferences;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        long timeSpanStart = cursor.getLong(cursor.getColumnIndex(AccountabilityHoursColumns.START));

        Calendar tmpCalendar = Calendar.getInstance();
        tmpCalendar.setTimeInMillis(timeSpanStart * 1000);
        tmpCalendar.add(Calendar.MINUTE, 90);
        long timeSpanEnd = tmpCalendar.getTimeInMillis() / 1000;

        String timeSpanString = Utility.formatTime(timeSpanStart) + " - " + Utility.formatTime(timeSpanEnd);
        viewHolder.mHoursTextView.setText(timeSpanString);

        int id = cursor.getInt(cursor.getColumnIndex(AccountabilityHoursColumns._ID));
        viewHolder.mTasksCursorAdapter.swapCursor(mTaskCursorList.get(id));

        viewHolder.mTasksRecyclerView.setLayoutManager(new CustomLinearLayoutManager(mContext));
        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mTasksCursorAdapter);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accountability_hours, parent, false);
        return new ViewHolder(itemView);
    }

    public void setTaskCursorList(List<MatrixCursor> taskCursorList) {
        mTaskCursorList = taskCursorList;
    }

    public interface AccountabilityHoursColumns {
        String _ID = "_id";
        String START = "START";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mHoursTextView;
        public RecyclerView mTasksRecyclerView;
        public View mView;
        public AccountabilityTasksCursorAdapter mTasksCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mHoursTextView = (TextView) view.findViewById(R.id.hours_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tasks_recycler_view);
            mTasksCursorAdapter = new AccountabilityTasksCursorAdapter(mContext, null, mViewHolder, mSharedPreferences);
        }
    }


}
