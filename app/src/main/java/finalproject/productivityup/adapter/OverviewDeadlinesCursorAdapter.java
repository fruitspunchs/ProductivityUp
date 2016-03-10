package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineTasksColumns;

/**
 * Adapter for deadlines overview card.
 */
public class OverviewDeadlinesCursorAdapter extends CursorRecyclerViewAdapter<OverviewDeadlinesCursorAdapter.ViewHolder> {
    ViewHolder mVh;
    private Context mContext;

    public OverviewDeadlinesCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(DeadlineTasksColumns.TASK)));
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overview_deadlines_list, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTaskTextView;

        public ViewHolder(View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.deadlines_task_recycler_view);
        }
    }
}
