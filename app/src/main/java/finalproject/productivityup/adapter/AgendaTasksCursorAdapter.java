package finalproject.productivityup.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.data.AgendaDaysColumns;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;

/**
 * Cursor adapter used to display deadline tasks
 */
public class AgendaTasksCursorAdapter extends CursorRecyclerViewAdapter<AgendaTasksCursorAdapter.AgendaTasksViewHolder> {
    private final String AGENDA_LAST_SELECTED_ITEM_KEY = "AGENDA_LAST_SELECTED_ITEM_KEY";
    private ImageButton mLastSelectedEditButton;
    private ImageButton mLastSelectedDeleteButton;
    private View mLastSelectedView;
    private TextView mLastSelectedTaskTextView;
    private CheckBox mLastSelectedCheckBox;
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private long mLastSelectedItem = -1;

    public AgendaTasksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(AgendaTasksViewHolder viewHolder, Cursor cursor) {
        boolean isChecked;
        int check = cursor.getInt(cursor.getColumnIndex(AgendaTasksColumns.IS_CHECKED));

        switch (check) {
            case 0:
                isChecked = false;
                break;
            default:
                isChecked = true;
                break;
        }
        viewHolder.mId = cursor.getLong(cursor.getColumnIndex(AgendaTasksColumns._ID));
        viewHolder.mDay = cursor.getLong(cursor.getColumnIndex(AgendaTasksColumns.DATE));
        viewHolder.mTask = cursor.getString(cursor.getColumnIndex(AgendaTasksColumns.TASK));

        viewHolder.mCheckBox.setChecked(isChecked);
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(AgendaTasksColumns.TASK)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onViewAttachedToWindow(AgendaTasksViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        Log.d(getClass().getSimpleName(), "Attaching to window: " + holder.mId);
        if (mLastSelectedItem == holder.mId) {
            Log.d(getClass().getSimpleName(), "Selection match: " + holder.mId);

            mLastSelectedDeleteButton = holder.mDeleteButton;
            mLastSelectedEditButton = holder.mEditButton;
            mLastSelectedView = holder.itemView;
            mLastSelectedTaskTextView = holder.mTaskTextView;
            mLastSelectedCheckBox = holder.mCheckBox;

            holder.mEditButton.setVisibility(View.VISIBLE);
            holder.mDeleteButton.setVisibility(View.VISIBLE);
            holder.mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.mCheckBox.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.itemView.setSelected(true);
        } else {
            holder.mEditButton.setVisibility(View.GONE);
            holder.mDeleteButton.setVisibility(View.GONE);
            holder.itemView.setSelected(false);
            holder.mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
            holder.mCheckBox.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
    }

    @Override
    public AgendaTasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agenda_list, parent, false);
        return new AgendaTasksViewHolder(itemView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(getClass().getSimpleName(), "Attaching to recycler view");
        mLastSelectedItem = mSharedPreferences.getLong(AGENDA_LAST_SELECTED_ITEM_KEY, -1);
    }

    public class AgendaTasksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTaskTextView;
        public ImageButton mEditButton;
        public ImageButton mDeleteButton;
        public CheckBox mCheckBox;
        public long mId;
        public long mDay;
        public String mTask;

        public AgendaTasksViewHolder(final View view) {
            super(view);

            mCheckBox = (CheckBox) view.findViewById(R.id.check_box);
            mTaskTextView = (TextView) view.findViewById(R.id.task_text_view);
            mEditButton = (ImageButton) view.findViewById(R.id.edit_button);
            mDeleteButton = (ImageButton) view.findViewById(R.id.delete_button);
            view.setOnClickListener(this);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] taskArg = {String.valueOf(mId)};
                    mContext.getContentResolver().delete(ProductivityProvider.AgendaTasks.CONTENT_URI, AgendaTasksColumns._ID + " = ?", taskArg);

                    if (getItemCount() == 1) {
                        String[] dayArg = {String.valueOf(mDay)};
                        mContext.getContentResolver().delete(ProductivityProvider.AgendaDays.CONTENT_URI, AgendaDaysColumns.DATE + " = ?", dayArg);
                    }
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 1/12/2016
//                    Intent intent = new Intent(mContext, EditDeadlineActivity.class);
//                    intent.putExtra(EditDeadlineActivity.ID_KEY, mId);
//                    intent.putExtra(EditDeadlineActivity.DATE_KEY, mDay);
//                    intent.putExtra(EditDeadlineActivity.TASK_KEY, mTask);
//                    mContext.startActivity(intent);
                }
            });

            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] taskArg = {String.valueOf(mId)};
                    ContentValues checkValues = new ContentValues();
                    int checkValue;

                    if (mCheckBox.isChecked()) {
                        checkValue = 1;
                    } else {
                        checkValue = 0;
                    }

                    checkValues.put(AgendaTasksColumns.IS_CHECKED, checkValue);
                    mContext.getContentResolver().update(ProductivityProvider.AgendaTasks.CONTENT_URI, checkValues, AgendaTasksColumns._ID + " = ?", taskArg);
                }
            });
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            if (mLastSelectedView != null) {
                mLastSelectedDeleteButton.setVisibility(View.GONE);
                mLastSelectedEditButton.setVisibility(View.GONE);
                mLastSelectedView.setSelected(false);
                mLastSelectedTaskTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
                mLastSelectedCheckBox.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }

            mLastSelectedDeleteButton = mDeleteButton;
            mLastSelectedEditButton = mEditButton;
            mLastSelectedView = v;
            mLastSelectedTaskTextView = mTaskTextView;
            mLastSelectedCheckBox = mCheckBox;

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            mCheckBox.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            v.setSelected(true);

            Log.d(getClass().getSimpleName(), "Item selected: " + mId);
            mLastSelectedItem = mId;
            mSharedPreferences.edit().putLong(AGENDA_LAST_SELECTED_ITEM_KEY, mId).apply();
        }
    }
}
