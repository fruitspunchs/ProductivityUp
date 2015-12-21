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
import finalproject.productivityup.libs.Utility;

/**
 * Created by User on 12/18/2015.
 */
public class DeadlineTasksCursorAdapter extends CursorRecyclerViewAdapter<DeadlineTasksCursorAdapter.ViewHolder> {
    ViewHolder mVh;

    public DeadlineTasksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.mTimeTextView.setText(Utility.formatTime(cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.TIME))));
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(DeadlineTasksColumns.TASK)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_list, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTimeTextView;
        public TextView mTaskTextView;

        public ViewHolder(View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.deadlines_task_text_view);
            mTimeTextView = (TextView) view.findViewById(R.id.deadlines_time_text_view);
        }
    }
}
