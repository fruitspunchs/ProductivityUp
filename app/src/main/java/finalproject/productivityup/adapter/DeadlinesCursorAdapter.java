package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.Utility;
import finalproject.productivityup.data.DeadlinesColumns;

/**
 * Created by User on 12/17/2015.
 */
public class DeadlinesCursorAdapter extends CursorRecyclerViewAdapter<DeadlinesCursorAdapter.ViewHolder> {

    ViewHolder mVh;

    public DeadlinesCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.mDateTextView.setText(Utility.formatDate(cursor.getLong(
                cursor.getColumnIndex(DeadlinesColumns.DATE))));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public ListView mTasksListView;

        public ViewHolder(View view) {
            super(view);
            mDateTextView = (TextView) view.findViewById(R.id.deadlines_card_date_text_view);
            mTasksListView = (ListView) view.findViewById(R.id.deadlines_card_tasks_list_view);
        }
    }
}
