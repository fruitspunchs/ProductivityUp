package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import finalproject.productivityup.R;
import finalproject.productivityup.libs.Utility;

/**
 * Adapter for accountability overview card.
 */
public class OverviewAccountabilityHoursCursorAdapter extends CursorRecyclerViewAdapter<OverviewAccountabilityHoursCursorAdapter.ViewHolder> {
    private Context mContext;
    private List<MatrixCursor> mTaskCursorList;

    public OverviewAccountabilityHoursCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(OverviewAccountabilityHoursCursorAdapter.ViewHolder viewHolder, Cursor cursor) {
        long timeSpanStart = cursor.getLong(cursor.getColumnIndex(AccountabilityHoursColumns.START));

        Calendar tmpCalendar = Calendar.getInstance();
        tmpCalendar.setTimeInMillis(timeSpanStart * 1000);
        tmpCalendar.add(Calendar.MINUTE, 90);
        long timeSpanEnd = tmpCalendar.getTimeInMillis() / 1000;

        String timeSpanString = Utility.formatTime(timeSpanStart) + " - " + Utility.formatTime(timeSpanEnd);
        viewHolder.mHoursTextView.setText(timeSpanString);

        int id = cursor.getInt(cursor.getColumnIndex(AccountabilityHoursColumns._ID));
        viewHolder.mTasksCursorAdapter.swapCursor(mTaskCursorList.get(id));

        viewHolder.mTasksRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        viewHolder.mTasksRecyclerView.setAdapter(viewHolder.mTasksCursorAdapter);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overview_accountability_hours, parent, false);
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
        public OverviewAccountabilityTasksCursorAdapter mTasksCursorAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mHoursTextView = (TextView) view.findViewById(R.id.hours_text_view);
            mTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tasks_recycler_view);
            mTasksCursorAdapter = new OverviewAccountabilityTasksCursorAdapter(mContext, null);
        }
    }
}
