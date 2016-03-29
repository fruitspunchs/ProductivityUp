package finalproject.productivityup.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.ui.agenda.AgendaActivity;
import finalproject.productivityup.ui.deadlines.DeadlinesActivityFragment;

/**
 * Adapter for agenda overview card.
 */
public class OverviewAgendaCursorAdapter extends CursorRecyclerViewAdapter<OverviewAgendaCursorAdapter.ViewHolder> {
    private Context mContext;

    public OverviewAgendaCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(OverviewAgendaCursorAdapter.ViewHolder viewHolder, Cursor cursor) {
        viewHolder.mTaskTextView.setText(cursor.getString(cursor.getColumnIndex(AgendaTasksColumns.TASK)));
        viewHolder.mId = cursor.getLong(cursor.getColumnIndex(AgendaTasksColumns._ID));

        int checkValue = cursor.getInt(cursor.getColumnIndex(AgendaTasksColumns.IS_CHECKED));
        boolean isChecked;

        switch (checkValue) {
            case 0:
                isChecked = false;
                break;
            default:
                isChecked = true;
                break;
        }

        viewHolder.mCheckBox.setChecked(isChecked);
    }

    @Override
    public OverviewAgendaCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overview_agenda_list, parent, false);
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTaskTextView;
        public CheckBox mCheckBox;
        public long mId;

        public ViewHolder(View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.task_text_view);
            mCheckBox = (CheckBox) view.findViewById(R.id.check_box);

            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int checkValue;

                    if (mCheckBox.isChecked()) {
                        checkValue = 1;
                    } else {
                        checkValue = 0;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putLong(UpdateCheckTask.ID_KEY, mId);
                    bundle.putInt(UpdateCheckTask.CHECK_VALUE_KEY, checkValue);

                    new UpdateCheckTask().execute(bundle);
                }
            });

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, AgendaActivity.class);
            intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DAY);
            mContext.startActivity(intent);
        }
    }

    private class UpdateCheckTask extends AsyncTask<Bundle, Void, Void> {
        private static final String ID_KEY = "ID_KEY";
        private static final String CHECK_VALUE_KEY = "CHECK_VALUE_KEY";

        protected Void doInBackground(Bundle... bundles) {

            String[] taskArg = {String.valueOf(bundles[0].getLong(ID_KEY))};
            ContentValues checkValues = new ContentValues();
            checkValues.put(AgendaTasksColumns.IS_CHECKED, bundles[0].getInt(CHECK_VALUE_KEY));
            mContext.getContentResolver().update(ProductivityProvider.AgendaTasks.CONTENT_URI, checkValues, AgendaTasksColumns._ID + " = ?", taskArg);

            return null;
        }
    }
}
