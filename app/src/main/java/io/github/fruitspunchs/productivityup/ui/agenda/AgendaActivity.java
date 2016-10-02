/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.agenda;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.data.AgendaTasksColumns;
import io.github.fruitspunchs.productivityup.data.DeadlineDaysColumns;
import io.github.fruitspunchs.productivityup.data.ProductivityProvider;
import io.github.fruitspunchs.productivityup.libs.Utility;

public class AgendaActivity extends AppCompatActivity {
    public static final String BATCH_KEY = "BATCH_KEY";
    public static final String ACTION_ADD_BATCH = "ACTION_ADD_BATCH";
    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.add_agenda_fab)
    FloatingActionButton mAddFab;

    @OnClick(R.id.add_agenda_fab)
    void clickAddFab() {
        Intent intent = new Intent(this, AddAgendaActivity.class);
        startActivityForResult(intent, 0);
    }

    public void onViewAttachedToWindow(long unixDate) {
        AgendaActivityFragment fragment = (AgendaActivityFragment) getSupportFragmentManager().findFragmentById(R.id.agenda_fragment);
        fragment.onViewAttachedToWindow(unixDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        ButterKnife.bind(this);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (ACTION_ADD_BATCH.equals(action)) {
            Bundle bundle = new Bundle();
            bundle.putString(InsertTask.BATCH_KEY, intent.getStringExtra(BATCH_KEY));

            new InsertTask().execute(bundle);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        AgendaActivityFragment fragment = (AgendaActivityFragment) getSupportFragmentManager().findFragmentById(R.id.agenda_fragment);
        fragment.onResult(requestCode, resultCode, data);
    }

    private class InsertTask extends AsyncTask<Bundle, Void, Void> {
        private static final String BATCH_KEY = "BATCH_KEY";

        protected Void doInBackground(Bundle... bundles) {
            String batchString = bundles[0].getString(BATCH_KEY);

            if (batchString == null)
                return null;

            String[] strings = batchString.split("\n");
            long unixDate = Utility.getCurrentTimeInSeconds();

            List<ContentValues> agendaTasksArrayList = new ArrayList<>();

            for (String s : strings) {
                String trimmedString = s.trim();

                if (trimmedString.equals("")) {
                    continue;
                }

                ContentValues cv = new ContentValues();

                cv.put(AgendaTasksColumns.DATE, unixDate);
                cv.put(AgendaTasksColumns.TASK, trimmedString);
                cv.put(AgendaTasksColumns.IS_CHECKED, 0);

                agendaTasksArrayList.add(cv);
            }

            if (agendaTasksArrayList.size() == 0) {
                return null;
            }

            ContentValues agendaDays = new ContentValues();
            agendaDays.put(DeadlineDaysColumns.DATE, unixDate);

            getContentResolver().insert(ProductivityProvider.AgendaDays.CONTENT_URI, agendaDays);
            ContentValues[] contentValues = new ContentValues[agendaTasksArrayList.size()];
            getContentResolver().bulkInsert(ProductivityProvider.AgendaTasks.CONTENT_URI, agendaTasksArrayList.toArray(contentValues));

            return null;
        }
    }

}
