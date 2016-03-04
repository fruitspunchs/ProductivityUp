package finalproject.productivityup.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import finalproject.productivityup.R;
import finalproject.productivityup.ui.accountability.AccountabilityActivity;
import finalproject.productivityup.ui.agenda.AgendaActivity;
import finalproject.productivityup.ui.deadlines.DeadlinesActivity;
import finalproject.productivityup.ui.deadlines.DeadlinesActivityFragment;

// TODO: 1/24/2016 ask if deadline was met, or reschedule or archive
// TODO: 1/24/2016 add animations to card resize
public class MainActivity extends AppCompatActivity {
    public static final long CALENDAR_MIN_DATE = 1451606400000L;
    public static final String CARD_TITLE_TOGGLE_KEY = "CARD_TITLE_TOGGLE_KEY";
    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.overview_card_deadlines)
    CardView mDeadlinesCardView;
    @Bind(R.id.overview_card_agenda)
    CardView mAgendaCardView;
    @Bind(R.id.overview_card_accountability_chart)
    CardView mAccountabilityChartCard;
    @Bind({R.id.deadlines_title_text_view, R.id.accountability_chart_title1_text_view, R.id.accountability_chart_title2_text_view, R.id.agenda_title_text_view, R.id.ultradian_rhythm_title_text_view, R.id.pomodoro_timer_title_text_view})
    List<TextView> mCardTitles;
    boolean mIsShowingCardTitles = true;
    @Bind(R.id.deadlines_task_recycler_view)
    RecyclerView mDeadlinesTaskRecyclerView;
    @Bind(R.id.deadlines_date_text_view)
    TextView mDeadlinesTimeLeftTextView;
    @Bind(R.id.deadlines_card_container)
    LinearLayout mDeadlinesCardContainer;
    @Bind(R.id.deadlines_no_item)
    TextView mDeadlinesNoItemTextView;
    @Bind(R.id.ultradian_rhythm_work_rest_button)
    ImageButton mUltradianRhythmWorkRestButton;
    @Bind(R.id.ultradian_rhythm_timer_text_view)
    TextView mUltradianRhythmTimerTextView;
    @Bind(R.id.pomodoro_timer_timer_text_view)
    TextView mPomodoroTimerTextView;
    @Bind(R.id.pomodoro_timer_start_pause_button)
    ImageButton mPomodoroTimerStartPauseImageButton;
    @Bind(R.id.agenda_card_recycler_view)
    RecyclerView mAgendaCardRecyclerView;
    @Bind(R.id.agenda_no_item_text_view)
    TextView mAgendaNoItemTextView;
    @Bind(R.id.accountability_card_recycler_view)
    RecyclerView mAccountabilityCardRecyclerView;
    @Bind(R.id.accountability_no_item_text_view)
    TextView mAccountabilityNoItemTextView;

    SharedPreferences mSharedPreferences;
    private PomodoroTimerCard mPomodoroTimerCard;
    private DeadlinesCard mDeadlinesCard;
    private AgendaCard mAgendaCard;
    private AccountabilityCard mAccountabilityCard;

    @OnClick(R.id.overview_card_deadlines)
    void clickDeadlinesCard() {
        Intent intent = new Intent(this, DeadlinesActivity.class);
        intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DEADLINE);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_agenda)
    void clickAgendaCard() {
        Intent intent = new Intent(this, AgendaActivity.class);
        intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DEADLINE);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_accountability_chart)
    void clickAccountabilityChartCard() {
        Intent intent = new Intent(this, AccountabilityActivity.class);
        intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DEADLINE);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        mDeadlinesCard = new DeadlinesCard(this, getSupportLoaderManager(), mDeadlinesTaskRecyclerView, mDeadlinesTimeLeftTextView, mDeadlinesNoItemTextView, mDeadlinesCardContainer);
        mDeadlinesCard.onCreate();

        UltradianRhythmTimerCard ultradianRhythmTimerCard = new UltradianRhythmTimerCard(this, mUltradianRhythmWorkRestButton, mUltradianRhythmTimerTextView);
        ultradianRhythmTimerCard.startTimer();

        mPomodoroTimerCard = new PomodoroTimerCard(this, mPomodoroTimerStartPauseImageButton, mPomodoroTimerTextView);
        mPomodoroTimerCard.initialize();

        mAgendaCard = new AgendaCard(this, getSupportLoaderManager(), mAgendaCardRecyclerView, mAgendaNoItemTextView);
        mAgendaCard.onCreate();

        mAccountabilityCard = new AccountabilityCard(this, getSupportLoaderManager(), mAccountabilityCardRecyclerView, mAccountabilityNoItemTextView);
        mAccountabilityCard.onCreate();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsShowingCardTitles = mSharedPreferences.getBoolean(CARD_TITLE_TOGGLE_KEY, true);

        if (mIsShowingCardTitles) {
            Log.d(LOG_TAG, "Showing card titles");
            for (TextView textView : mCardTitles) {
                textView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(LOG_TAG, "Hiding card titles");
            for (TextView textView : mCardTitles) {
                textView.setVisibility(View.GONE);
            }
        }
        mDeadlinesCard.toggleCardTitles(mIsShowingCardTitles);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Restarting loader");
        mDeadlinesCard.onStart();
        mAgendaCard.onStart();
        mAccountabilityCard.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem cardTitleToggleIcon = menu.findItem(R.id.action_toggle_card_title);

        if (mIsShowingCardTitles) {
            cardTitleToggleIcon.setIcon(R.drawable.ic_hide_card_title);
        } else {
            cardTitleToggleIcon.setIcon(R.drawable.ic_show_card_title);
        }

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
            toggleCardTitles(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleCardTitles(MenuItem item) {
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
        mDeadlinesCard.toggleCardTitles(mIsShowingCardTitles);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPomodoroTimerCard.onPause();
        mSharedPreferences.edit().putBoolean(CARD_TITLE_TOGGLE_KEY, mIsShowingCardTitles).apply();
    }

    public interface CURSOR_LOADER_ID {
        int DEADLINE_TASKS = 0;
        int NEXT_DEADLINE = 1;
        int AGENDA = 2;
        int ACCOUNTABILITY = 3;
    }
}
