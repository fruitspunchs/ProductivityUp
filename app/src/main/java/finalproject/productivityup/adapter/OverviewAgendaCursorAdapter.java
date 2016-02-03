package finalproject.productivityup.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import finalproject.productivityup.R;
import finalproject.productivityup.data.AgendaTasksColumns;
import finalproject.productivityup.ui.agenda.AgendaActivity;
import finalproject.productivityup.ui.deadlines.DeadlinesActivityFragment;

/**
 * Created by User on 1/26/2016.
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

        public ViewHolder(View view) {
            super(view);
            mTaskTextView = (TextView) view.findViewById(R.id.task_text_view);
            mCheckBox = (CheckBox) view.findViewById(R.id.check_box);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, AgendaActivity.class);
            intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DEADLINE);
            mContext.startActivity(intent);
        }
    }
}
