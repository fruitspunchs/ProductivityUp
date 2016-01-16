package finalproject.productivityup.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import finalproject.productivityup.R;
import finalproject.productivityup.data.DeadlineDaysColumns;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.deadlines.DeadlinesActivity;
import finalproject.productivityup.ui.deadlines.EditDeadlineActivity;

/**
 * Cursor adapter used to display deadline tasks
 */
public class DeadlineTasksCursorAdapter extends CursorRecyclerViewAdapter<DeadlineTasksCursorAdapter.DeadlineTasksViewHolder> {
    private final String DEADLINES_LAST_SELECTED_ITEM_KEY = "DEADLINES_LAST_SELECTED_ITEM_KEY";
    private final DeadlineTasksLastSelectedItemViewHolder mLastSelectedViewHolder;
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public DeadlineTasksCursorAdapter(Context context, Cursor cursor, DeadlineTasksLastSelectedItemViewHolder lastSelectedViewHolder) {
        super(context, cursor);
        mContext = context;
        mLastSelectedViewHolder = lastSelectedViewHolder;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @SuppressWarnings("deprecation")
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

        viewHolder.mId = cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns._ID));
        viewHolder.mDay = cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.DATE));
        viewHolder.mTask = cursor.getString(cursor.getColumnIndex(DeadlineTasksColumns.TASK));
        viewHolder.mTime = cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.TIME));

        viewHolder.mTimeTextView.setText(Utility.formatTime(cursor.getLong(cursor.getColumnIndex(DeadlineTasksColumns.TIME))));
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(DeadlineTasksColumns.TASK)));

        viewHolder.mTaskTextView.setTextColor(textColor);
        viewHolder.mTimeTextView.setTextColor(textColor);
    }

    @Override
    public DeadlineTasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadlines_list, parent, false);
        return new DeadlineTasksViewHolder(itemView);
    }

    @Override
    public void onViewAttachedToWindow(DeadlineTasksViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        Log.d(getClass().getSimpleName(), "Attaching to window: " + holder.mId);
        if (mLastSelectedViewHolder.mLastSelectedItem == holder.mId) {
            Log.d(getClass().getSimpleName(), "Selection match: " + holder.mId);

            mLastSelectedViewHolder.mLastSelectedDeleteButton = holder.mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = holder.mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = holder.itemView;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = holder.mTaskTextView;
            mLastSelectedViewHolder.mLastSelectedTimeTextView = holder.mTimeTextView;
            mLastSelectedViewHolder.mLastSelectedTextColor = holder.mTaskTextView.getCurrentTextColor();

            holder.mEditButton.setVisibility(View.VISIBLE);
            holder.mDeleteButton.setVisibility(View.VISIBLE);
            holder.mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.mTimeTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.itemView.setSelected(true);
        }

        ((DeadlinesActivity) mContext).scrollToPosition(holder.mDay);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(getClass().getSimpleName(), "Attaching to recycler view");
        mLastSelectedViewHolder.mLastSelectedItem = mSharedPreferences.getLong(DEADLINES_LAST_SELECTED_ITEM_KEY, -1);

    }

    public static class DeadlineTasksLastSelectedItemViewHolder {
        private ImageButton mLastSelectedEditButton;
        private ImageButton mLastSelectedDeleteButton;
        private View mLastSelectedView;
        private TextView mLastSelectedTimeTextView;
        private TextView mLastSelectedTaskTextView;
        private int mLastSelectedTextColor;
        private long mLastSelectedItem = -1;
    }

    public class DeadlineTasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTimeTextView;
        public TextView mTaskTextView;
        public ImageButton mEditButton;
        public ImageButton mDeleteButton;
        public long mId;
        public long mDay;
        public String mTask;
        public long mTime;

        public DeadlineTasksViewHolder(final View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.deadlines_task_text_view);
            mTimeTextView = (TextView) view.findViewById(R.id.deadlines_time_text_view);
            mEditButton = (ImageButton) view.findViewById(R.id.deadlines_edit_button);
            mDeleteButton = (ImageButton) view.findViewById(R.id.deadlines_delete_button);
            view.setOnClickListener(this);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] taskArg = {String.valueOf(mId)};
                    mContext.getContentResolver().delete(ProductivityProvider.DeadlineTasks.CONTENT_URI, DeadlineTasksColumns._ID + " = ?", taskArg);

                    if (getItemCount() == 1) {
                        String[] dayArg = {String.valueOf(mDay)};
                        mContext.getContentResolver().delete(ProductivityProvider.DeadlineDays.CONTENT_URI, DeadlineDaysColumns.DATE + " = ?", dayArg);
                    }

                    mDeleteButton.setVisibility(View.GONE);
                    mEditButton.setVisibility(View.GONE);
                    view.setSelected(false);

                    mLastSelectedViewHolder.mLastSelectedDeleteButton = null;
                    mLastSelectedViewHolder.mLastSelectedEditButton = null;
                    mLastSelectedViewHolder.mLastSelectedView = null;
                    mLastSelectedViewHolder.mLastSelectedTaskTextView = null;
                    mLastSelectedViewHolder.mLastSelectedTimeTextView = null;
                    mLastSelectedViewHolder.mLastSelectedTextColor = 0;
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditDeadlineActivity.class);
                    intent.putExtra(EditDeadlineActivity.ID_KEY, mId);
                    intent.putExtra(EditDeadlineActivity.TIME_KEY, mTime);
                    intent.putExtra(EditDeadlineActivity.DATE_KEY, mDay);
                    intent.putExtra(EditDeadlineActivity.TASK_KEY, mTask);
                    mContext.startActivity(intent);
                }
            });
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            if (mLastSelectedViewHolder.mLastSelectedView != null) {
                mLastSelectedViewHolder.mLastSelectedDeleteButton.setVisibility(View.GONE);
                mLastSelectedViewHolder.mLastSelectedEditButton.setVisibility(View.GONE);
                mLastSelectedViewHolder.mLastSelectedView.setSelected(false);
                mLastSelectedViewHolder.mLastSelectedTimeTextView.setTextColor(mLastSelectedViewHolder.mLastSelectedTextColor);
                mLastSelectedViewHolder.mLastSelectedTaskTextView.setTextColor(mLastSelectedViewHolder.mLastSelectedTextColor);
            }

            mLastSelectedViewHolder.mLastSelectedDeleteButton = mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = v;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = mTaskTextView;
            mLastSelectedViewHolder.mLastSelectedTimeTextView = mTimeTextView;
            mLastSelectedViewHolder.mLastSelectedTextColor = mTaskTextView.getCurrentTextColor();

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            mTimeTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            v.setSelected(true);
            mLastSelectedViewHolder.mLastSelectedItem = mId;
            mSharedPreferences.edit().putLong(DEADLINES_LAST_SELECTED_ITEM_KEY, mId).apply();
        }
    }
}
