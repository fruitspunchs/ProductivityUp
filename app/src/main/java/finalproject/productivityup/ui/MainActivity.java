package finalproject.productivityup.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import finalproject.productivityup.R;
import finalproject.productivityup.adapter.OverviewDeadlinesCursorAdapter;
import finalproject.productivityup.data.DeadlineTasksColumns;
import finalproject.productivityup.data.ProductivityProvider;
import finalproject.productivityup.libs.CustomLinearLayoutManager;
import finalproject.productivityup.libs.Utility;
import finalproject.productivityup.ui.accountability.AccountabilityChartActivity;
import finalproject.productivityup.ui.agenda.AgendaActivity;
import finalproject.productivityup.ui.deadlines.DeadlinesActivity;
import finalproject.productivityup.ui.pomodoro.PomodoroTimerActivity;
import finalproject.productivityup.ui.quiz.ProductivityQuizActivity;
import finalproject.productivityup.ui.rhythm.UltradianRhythmActivity;

//TODO: Feature: ask if deadline was met, or reschedule or archive
//TODO: Feature: add animations to card resize
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DEADLINE_TASKS_CURSOR_LOADER_ID = 0;
    private static final int NEXT_DEADLINE_CURSOR_LOADER_ID = 1;
    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.overview_card_pomodoro_timer)
    CardView mPomodoroTimerCard;
    @Bind(R.id.overview_card_ultradian_rhythm)
    CardView mUltradianRhythmCard;
    @Bind(R.id.overview_card_deadlines)
    CardView mDeadlinesCard;
    @Bind(R.id.overview_card_agenda)
    CardView mAgendaCard;
    @Bind(R.id.overview_card_accountability_chart)
    CardView mAccountabilityChartCard;
    @Bind(R.id.overview_card_productivity_quiz)
    CardView mProductivityQuizCard;
    @Bind({R.id.deadlines_title_text_view, R.id.accountability_chart_title1_text_view, R.id.accountability_chart_title2_text_view, R.id.agenda_title_text_view, R.id.ultradian_rhythm_title_text_view, R.id.pomodoro_timer_title_text_view})
    List<TextView> mCardTitles;
    boolean mIsShowingCardTitles = true;
    @Bind(R.id.deadlines_task_text_view)
    RecyclerView mDeadlinesTaskRecyclerView;
    @Bind(R.id.deadlines_date_text_view)
    TextView mDeadlinesTimeLeft;
    @Bind(R.id.deadlines_card_container)
    LinearLayout mDeadlinesCardContainer;
    @Bind(R.id.deadlines_no_item)
    TextView mDeadlinesNoItem;
    boolean mHasDeadlines = true;


    OverviewDeadlinesCursorAdapter mOverviewDeadlinesCursorAdapter;
    private long nextDeadlineUnixTime = -1;
    private CountDownTimer deadlinesCountdownTimer;
    private CountDownTimer mDeadlineTimeUpDelayCountDownTimer;

    @OnClick(R.id.overview_card_pomodoro_timer)
    void clickPomodoroCard() {
        Intent intent = new Intent(this, PomodoroTimerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_ultradian_rhythm)
    void clickUltradianCard() {
        Intent intent = new Intent(this, UltradianRhythmActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_deadlines)
    void clickDeadlinesCard() {
        Intent intent = new Intent(this, DeadlinesActivity.class);
        startActivity(intent);

    }

    @OnClick(R.id.overview_card_agenda)
    void clickAgendaCard() {
        Intent intent = new Intent(this, AgendaActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_accountability_chart)
    void clickAccountabilityChartCard() {
        Intent intent = new Intent(this, AccountabilityChartActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_productivity_quiz)
    void clickProductivityQuizCard() {
        Intent intent = new Intent(this, ProductivityQuizActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, this);
        mDeadlinesTaskRecyclerView.setLayoutManager(new CustomLinearLayoutManager(this));
        mOverviewDeadlinesCursorAdapter = new OverviewDeadlinesCursorAdapter(this, null);
        mDeadlinesTaskRecyclerView.setAdapter(mOverviewDeadlinesCursorAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Restarting loader");
        getSupportLoaderManager().restartLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_toggle_card_title) {
            if (mIsShowingCardTitles) {
                Log.d(LOG_TAG, "Hiding card titles");
                for (TextView textView : mCardTitles) {
                    textView.setVisibility(View.GONE);
                }
                item.setIcon(R.drawable.ic_show_card_title);
                mIsShowingCardTitles = false;
            } else {
                Log.d(LOG_TAG, "Showing card titles");
                for (TextView textView : mCardTitles) {
                    textView.setVisibility(View.VISIBLE);
                }
                item.setIcon(R.drawable.ic_hide_card_title);
                mIsShowingCardTitles = true;
            }

            adjustDeadlinesLayout(mIsShowingCardTitles);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == DEADLINE_TASKS_CURSOR_LOADER_ID) {
            Calendar currentTime = Calendar.getInstance();
            long currentTimeInSeconds = currentTime.getTimeInMillis() / 1000;
            String[] selectionArgs = {String.valueOf(currentTimeInSeconds)};

            return new CursorLoader(this, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " > ?",
                    selectionArgs,
                    DeadlineTasksColumns.TIME + " ASC");
        } else if (id == NEXT_DEADLINE_CURSOR_LOADER_ID) {
            String[] selectionArgs = {String.valueOf(nextDeadlineUnixTime)};

            return new CursorLoader(this, ProductivityProvider.DeadlineTasks.CONTENT_URI,
                    null,
                    DeadlineTasksColumns.TIME + " = ?",
                    selectionArgs,
                    null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Loader finished. Id: " + loader.getId());

        if (loader.getId() == NEXT_DEADLINE_CURSOR_LOADER_ID) {
            mOverviewDeadlinesCursorAdapter.swapCursor(data);
        } else if (loader.getId() == DEADLINE_TASKS_CURSOR_LOADER_ID) {
            if (data.moveToNext()) {

                mHasDeadlines = true;

                mDeadlinesTimeLeft.setVisibility(View.VISIBLE);
                mDeadlinesTaskRecyclerView.setVisibility(View.VISIBLE);
                mDeadlinesNoItem.setVisibility(View.GONE);

                nextDeadlineUnixTime = data.getLong(data.getColumnIndex(DeadlineTasksColumns.TIME));

                long timeUntilNextDeadline = nextDeadlineUnixTime * 1000 - System.currentTimeMillis();

                if (null != deadlinesCountdownTimer) {
                    deadlinesCountdownTimer.cancel();
                }

                deadlinesCountdownTimer = new CountDownTimer(timeUntilNextDeadline, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        mDeadlinesTimeLeft.setText(Utility.formatTimeLeft(getApplication(), millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        mDeadlinesTimeLeft.setText(getApplication().getString(R.string.time_up));
                        restartDeadlinesLoader();
                        Log.d(LOG_TAG, "Countdown finished");
                    }
                }.start();

                getSupportLoaderManager().restartLoader(NEXT_DEADLINE_CURSOR_LOADER_ID, null, this);

                adjustDeadlinesLayout(mIsShowingCardTitles);

            } else {
                mDeadlinesTimeLeft.setVisibility(View.GONE);
                mDeadlinesTaskRecyclerView.setVisibility(View.GONE);
                mDeadlinesNoItem.setVisibility(View.VISIBLE);

                mHasDeadlines = false;

                adjustDeadlinesLayout(mIsShowingCardTitles);

            }
        }
    }

    private void adjustDeadlinesLayout(boolean isShowingCardTitles) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        if (isShowingCardTitles || mHasDeadlines) {
            layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_padding_bottom));
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        mDeadlinesCardContainer.setLayoutParams(layoutParams);
    }

    private void restartDeadlinesLoader() {
        Log.d(LOG_TAG, "Waiting a few seconds before restarting loader");

        if (null != mDeadlineTimeUpDelayCountDownTimer) {
            mDeadlineTimeUpDelayCountDownTimer.cancel();
        }

        final MainActivity thisActivity = this;

        mDeadlineTimeUpDelayCountDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                getSupportLoaderManager().restartLoader(DEADLINE_TASKS_CURSOR_LOADER_ID, null, thisActivity);
            }
        }.start();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mOverviewDeadlinesCursorAdapter.swapCursor(null);
    }
}
