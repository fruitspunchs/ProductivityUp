package finalproject.productivityup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.libs.Utility;

/**
 * Created by User on 12/18/2015.
 */
public class DeadlineTasksCursorAdapter extends CursorRecyclerViewAdapter<DeadlineTasksCursorAdapter.DeadlineTasksViewHolder> {
    private static ImageButton sLastSelectedEditButton;
    private static ImageButton sLastSelectedDeleteButton;
    private static View sLastSelectedView;
    private static TextView sLastSelectedTimeTextView;
    private static TextView sLastSelectedTaskTextView;
    private static int sLastSelectedTextColor;
    private DeadlineTasksViewHolder mVh;
    private Context mContext;
    private DeadlineTasksCursorAdapterOnClickHandler mDeadlineTasksCursorAdapterOnClickHandler;

    public DeadlineTasksCursorAdapter(Context context, DeadlineTasksCursorAdapterOnClickHandler deadlineTasksCursorAdapterOnClickHandler, Cursor cursor) {
        super(context, cursor);
        mDeadlineTasksCursorAdapterOnClickHandler = deadlineTasksCursorAdapterOnClickHandler;
    }

    @Override
    public void onBindViewHolder(DeadlineTasksViewHolder viewHolder, Cursor cursor) {
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
    public DeadlineTasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_list, parent, false);
        DeadlineTasksViewHolder vh = new DeadlineTasksViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    public interface DeadlineTasksCursorAdapterOnClickHandler {
        void onClick(long unixDate, String task, DeadlineTasksViewHolder deadlineTasksViewHolder);
    }

    public class DeadlineTasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTimeTextView;
        public TextView mTaskTextView;
        public ImageButton mEditButton;
        public ImageButton mDeleteButton;

        public DeadlineTasksViewHolder(View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.deadlines_task_text_view);
            mTimeTextView = (TextView) view.findViewById(R.id.deadlines_time_text_view);
            mEditButton = (ImageButton) view.findViewById(R.id.deadlines_edit_button);
            mDeleteButton = (ImageButton) view.findViewById(R.id.deadlines_delete_button);
            view.setOnClickListener(this);
        }

        //TODO: request layout on parent card
        //TODO: implement delete action
        //TODO: implement edit action
        @Override
        public void onClick(View v) {
            if (sLastSelectedView != null) {
                sLastSelectedDeleteButton.setVisibility(View.GONE);
                sLastSelectedEditButton.setVisibility(View.GONE);
                sLastSelectedView.setSelected(false);
                sLastSelectedTimeTextView.setTextColor(sLastSelectedTextColor);
                sLastSelectedTaskTextView.setTextColor(sLastSelectedTextColor);
            }

            sLastSelectedDeleteButton = mDeleteButton;
            sLastSelectedEditButton = mEditButton;
            sLastSelectedView = v;
            sLastSelectedTaskTextView = mTaskTextView;
            sLastSelectedTimeTextView = mTimeTextView;
            sLastSelectedTextColor = mTaskTextView.getCurrentTextColor();

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            mTimeTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            v.setSelected(true);

            mDeadlineTasksCursorAdapterOnClickHandler.onClick(0, "", this);
        }
    }
}
