/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.data.AgendaDaysColumns;
import io.github.fruitspunchs.productivityup.data.AgendaTasksColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;
import io.github.fruitspunchs.productivityup.ui.agenda.AgendaActivity;
import io.github.fruitspunchs.productivityup.ui.agenda.EditAgendaActivity;

/**
 * Adapter for agenda task items.
 */
public class AgendaTasksCursorAdapter extends CursorRecyclerViewAdapter<AgendaTasksCursorAdapter.AgendaTasksViewHolder> {
    public static final String AGENDA_LAST_SELECTED_ITEM_KEY = "AGENDA_LAST_SELECTED_ITEM_KEY";
    private final AgendaTasksLastSelectedItemViewHolder mLastSelectedViewHolder;
    private final SharedPreferences mSharedPreferences;
    private Context mContext;

    public AgendaTasksCursorAdapter(Context context, Cursor cursor, AgendaTasksLastSelectedItemViewHolder lastSelectedViewHolder, SharedPreferences sharedPreferences) {
        super(context, cursor);
        mContext = context;
        mLastSelectedViewHolder = lastSelectedViewHolder;
        mSharedPreferences = sharedPreferences;
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

    @SuppressWarnings("deprecation")
    @Override
    public void onViewAttachedToWindow(AgendaTasksViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        Log.d(getClass().getSimpleName(), "Attaching to window: " + holder.mId);
        if (mLastSelectedViewHolder.mLastSelectedItem == holder.mId) {
            Log.d(getClass().getSimpleName(), "Selection match: " + holder.mId);

            mLastSelectedViewHolder.mLastSelectedDeleteButton = holder.mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = holder.mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = holder.itemView;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = holder.mTaskTextView;
            mLastSelectedViewHolder.mLastSelectedCheckBox = holder.mCheckBox;

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

        ((AgendaActivity) mContext).onViewAttachedToWindow(holder.mDay);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(getClass().getSimpleName(), "Attaching to recycler view");
    }

    public static class AgendaTasksLastSelectedItemViewHolder {
        public long mLastSelectedItem = -1;
        public ImageButton mLastSelectedEditButton;
        public ImageButton mLastSelectedDeleteButton;
        public View mLastSelectedView;
        public TextView mLastSelectedTaskTextView;
        public CheckBox mLastSelectedCheckBox;
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

                    Bundle bundle = new Bundle();
                    bundle.putLong(DeleteTask.ID_KEY, mId);
                    bundle.putLong(DeleteTask.DAY_KEY, mDay);

                    new DeleteTask().execute(bundle);

                    mDeleteButton.setVisibility(View.GONE);
                    mEditButton.setVisibility(View.GONE);
                    view.setSelected(false);

                    mLastSelectedViewHolder.mLastSelectedDeleteButton = null;
                    mLastSelectedViewHolder.mLastSelectedEditButton = null;
                    mLastSelectedViewHolder.mLastSelectedView = null;
                    mLastSelectedViewHolder.mLastSelectedTaskTextView = null;
                    mLastSelectedViewHolder.mLastSelectedCheckBox = null;
                    mLastSelectedViewHolder.mLastSelectedItem = -1;
                }
            });

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EditAgendaActivity.class);
                    intent.putExtra(EditAgendaActivity.ID_KEY, mId);
                    intent.putExtra(EditAgendaActivity.DATE_KEY, mDay);
                    intent.putExtra(EditAgendaActivity.TASK_KEY, mTask);
                    int checkValue;
                    if (mCheckBox.isChecked()) {
                        checkValue = 1;
                    } else {
                        checkValue = 0;
                    }
                    intent.putExtra(EditAgendaActivity.CHECK_VALUE_KEY, checkValue);
                    ((Activity) mContext).startActivityForResult(intent, 0);
                }
            });

            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox.setFocusable(false);
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


            mDeleteButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int color = mContext.getResources().getColor(android.R.color.transparent);
                    if (hasFocus) {
                        color = mContext.getResources().getColor(R.color.lightGray);
                    }
                    mDeleteButton.setBackgroundColor(color);
                }
            });


            mEditButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int color = mContext.getResources().getColor(android.R.color.transparent);
                    if (hasFocus) {
                        color = mContext.getResources().getColor(R.color.lightGray);
                    }
                    mEditButton.setBackgroundColor(color);
                }
            });

            mCheckBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int color = mContext.getResources().getColor(R.color.cardViewBackgroundColor);
                    if (hasFocus) {
                        color = mContext.getResources().getColor(R.color.lightGray);
                    }
                    mCheckBox.setBackgroundColor(color);
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
                mLastSelectedViewHolder.mLastSelectedCheckBox.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                mLastSelectedViewHolder.mLastSelectedCheckBox.setFocusable(false);
            }

            mLastSelectedViewHolder.mLastSelectedDeleteButton = mDeleteButton;
            mLastSelectedViewHolder.mLastSelectedEditButton = mEditButton;
            mLastSelectedViewHolder.mLastSelectedView = v;
            mLastSelectedViewHolder.mLastSelectedTaskTextView = mTaskTextView;
            mLastSelectedViewHolder.mLastSelectedCheckBox = mCheckBox;

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mTaskTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            mCheckBox.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            mCheckBox.setFocusable(true);
            mCheckBox.requestFocus();
            v.setSelected(true);
            mSharedPreferences.edit().putLong(AGENDA_LAST_SELECTED_ITEM_KEY, mId).apply();
        }
    }

    private class DeleteTask extends AsyncTask<Bundle, Void, Void> {
        private static final String ID_KEY = "ID_KEY";
        private static final String DAY_KEY = "DAY_KEY";

        protected Void doInBackground(Bundle... bundles) {

            if (getItemCount() == 1) {
                String[] dayArg = {String.valueOf(bundles[0].getLong(DAY_KEY))};
                mContext.getContentResolver().delete(ProductivityProvider.AgendaDays.CONTENT_URI, AgendaDaysColumns.DATE + " = ?", dayArg);
            }

            String[] taskArg = {String.valueOf(bundles[0].getLong(ID_KEY))};
            mContext.getContentResolver().delete(ProductivityProvider.AgendaTasks.CONTENT_URI, AgendaTasksColumns._ID + " = ?", taskArg);

            return null;
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
