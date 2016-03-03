package finalproject.productivityup.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.data.AccountabilityTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.ui.accountability.AccountabilityActivity;
import finalproject.productivityup.ui.accountability.EditAccountabilityActivity;

/**
 * Cursor adapter used to display deadline tasks
 */
public class AccountabilityTasksCursorAdapter extends CursorRecyclerViewAdapter<AccountabilityTasksCursorAdapter.ViewHolder> {
    public static final String ACCOUNTABILITY_LAST_SELECTED_ITEM_KEY = "ACCOUNTABILITY_LAST_SELECTED_ITEM_KEY";
    private final LastSelectedItemViewHolder mLastSelectedViewHolder;
    private final SharedPreferences mSharedPreferences;
    private Context mContext;

    public AccountabilityTasksCursorAdapter(Context context, Cursor cursor, LastSelectedItemViewHolder lastSelectedViewHolder, SharedPreferences sharedPreferences) {
        super(context, cursor);
        mContext = context;
        mLastSelectedViewHolder = lastSelectedViewHolder;
        mSharedPreferences = sharedPreferences;
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
                .inflate(R.layout.item_accountability_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        Log.d(getClass().getSimpleName(), "Attaching to window: " + holder.mId);
        if (mLastSelectedViewHolder.mLastSelectedItem == holder.mId) {
            Log.d(getClass().getSimpleName(), "Selection match: " + holder.mId);

            mLastSelectedViewHolder.mLastSelectedDeleteButton = holder.mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = holder.mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = holder.itemView;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = holder.mTaskTextView;

            holder.mEditButton.setVisibility(View.VISIBLE);
            holder.mDeleteButton.setVisibility(View.VISIBLE);
            holder.mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.itemView.setSelected(true);
        } else {
            holder.mEditButton.setVisibility(View.GONE);
            holder.mDeleteButton.setVisibility(View.GONE);
            holder.itemView.setSelected(false);
            holder.mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
        }

        ((AccountabilityActivity) mContext).onViewAttachedToWindow(holder.mDay);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(getClass().getSimpleName(), "Attaching to recycler view");
    }

    public static class LastSelectedItemViewHolder {
        public long mLastSelectedItem = -1;
        public TextView mLastSelectedTaskTextView;
        private ImageButton mLastSelectedEditButton;
        private ImageButton mLastSelectedDeleteButton;
        private View mLastSelectedView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTaskTextView;
        public ImageButton mEditButton;
        public ImageButton mDeleteButton;
        public long mId;
        public long mDay;
        public String mTask;
        public long mTime;

        public ViewHolder(final View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.task_text_view);
            mEditButton = (ImageButton) view.findViewById(R.id.edit_button);
            mDeleteButton = (ImageButton) view.findViewById(R.id.delete_button);
            view.setOnClickListener(this);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] taskArg = {String.valueOf(mId)};
                    mContext.getContentResolver().delete(ProductivityProvider.AccountabilityChartTasks.CONTENT_URI, AccountabilityTasksColumns._ID + " = ?", taskArg);

                    // TODO: 3/1/2016 delete time span if cursor is empty

                    // TODO: 3/3/2016 query day if empty. if true, delete day

                    //// TODO: 3/3/2016 close all matrix cursors to fix leaks

                    mDeleteButton.setVisibility(View.GONE);
                    mEditButton.setVisibility(View.GONE);
                    view.setSelected(false);

                    mLastSelectedViewHolder.mLastSelectedDeleteButton = null;
                    mLastSelectedViewHolder.mLastSelectedEditButton = null;
                    mLastSelectedViewHolder.mLastSelectedView = null;
                    mLastSelectedViewHolder.mLastSelectedTaskTextView = null;
                    mLastSelectedViewHolder.mLastSelectedItem = -1;
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditAccountabilityActivity.class);
                    intent.putExtra(EditAccountabilityActivity.ID_KEY, mId);
                    intent.putExtra(EditAccountabilityActivity.TIME_KEY, mTime);
                    intent.putExtra(EditAccountabilityActivity.DATE_KEY, mDay);
                    intent.putExtra(EditAccountabilityActivity.TASK_KEY, mTask);
                    ((Activity) mContext).startActivityForResult(intent, 0);
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
                mLastSelectedViewHolder.mLastSelectedTaskTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            }

            mLastSelectedViewHolder.mLastSelectedDeleteButton = mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = v;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = mTaskTextView;

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            v.setSelected(true);
            mLastSelectedViewHolder.mLastSelectedItem = mId;
            mSharedPreferences.edit().putLong(ACCOUNTABILITY_LAST_SELECTED_ITEM_KEY, mId).apply();
        }
    }
}
