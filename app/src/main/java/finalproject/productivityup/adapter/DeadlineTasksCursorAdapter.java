package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.libs.Utility;

/**
 * Created by User on 12/18/2015.
 */
public class DeadlineTasksCursorAdapter extends CursorRecyclerViewAdapter<DeadlineTasksCursorAdapter.ViewHolder> {
    ViewHolder mVh;
    private Context mContext;

    public DeadlineTasksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        Calendar currentTime = Calendar.getInstance();
        long unixTime = cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.TIME));
        int textColor;

        if (unixTime < currentTime.getTimeInMillis() / 1000) {
            textColor = mContext.getResources().getColor(R.color.colorSecondaryText);
        } else {
            textColor = mContext.getResources().getColor(R.color.colorPrimaryText);
        }

        viewHolder.mTimeTextView.setText(Utility.formatTime(cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.TIME))));
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(DeadlineTasksColumns.TASK)));

        viewHolder.mTaskTextView.setTextColor(textColor);
        viewHolder.mTimeTextView.setTextColor(textColor);
    }

    public void setContext(Context context) {
        mContext = context;
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
