package finalproject.productivityup.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
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
    private ImageButton mLastSelectedEditButton;
    private ImageButton mLastSelectedDeleteButton;
    private View mLastSelectedView;
    private TextView mLastSelectedTaskTextView;
    private int mLastSelectedTextColor;
    private CheckBox mLastSelectedCheckBox;
    private Context mContext;

    public AgendaTasksCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
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

    @Override
    public AgendaTasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agenda_list, parent, false);
        return new AgendaTasksViewHolder(itemView);
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
                mLastSelectedTaskTextView.setTextColor(mLastSelectedTextColor);
                mLastSelectedCheckBox.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }

            mLastSelectedDeleteButton = mDeleteButton;
            mLastSelectedEditButton = mEditButton;
            mLastSelectedView = v;
            mLastSelectedTaskTextView = mTaskTextView;
            mLastSelectedTextColor = mTaskTextView.getCurrentTextColor();
            mLastSelectedCheckBox = mCheckBox;

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            mCheckBox.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            v.setSelected(true);
        }
    }
}
