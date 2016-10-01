package io.github.fruitspunchs.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.data.AccountabilityTasksColumns;

/**
 * Adapter for accountability overview card.
 */
public class OverviewAccountabilityTasksCursorAdapter extends CursorRecyclerViewAdapter<OverviewAccountabilityTasksCursorAdapter.ViewHolder> {
    private Context mContext;

    public OverviewAccountabilityTasksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        viewHolder.mId = cursor.getLong(cursor.getColumnIndex(AccountabilityTasksColumns._ID));
        viewHolder.mDay = cursor.getLong(cursor.getColumnIndex(AccountabilityTasksColumns.DATE));
        viewHolder.mTask = cursor.getString(cursor.getColumnIndex(AccountabilityTasksColumns.TASK));
        viewHolder.mTime = cursor.getLong(cursor.getColumnIndex(AccountabilityTasksColumns.TIME));

        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(AccountabilityTasksColumns.TASK)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overview_accountability_tasks_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(getClass().getSimpleName(), "Attaching to recycler view");
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTaskTextView;
        public long mId;
        public long mDay;
        public String mTask;
        public long mTime;

        public ViewHolder(final View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.task_text_view);
        }
    }
}
