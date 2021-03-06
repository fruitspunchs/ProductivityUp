/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import io.github.fruitspunchs.productivityup.R;
import io.github.fruitspunchs.productivityup.libs.Utility;
import io.github.fruitspunchs.productivityup.service.TimerService;
import io.github.fruitspunchs.productivityup.ui.accountability.AccountabilityActivity;
import io.github.fruitspunchs.productivityup.ui.accountability.AccountabilityActivityFragment;
import io.github.fruitspunchs.productivityup.ui.agenda.AgendaActivity;
import io.github.fruitspunchs.productivityup.ui.agenda.AgendaActivityFragment;
import io.github.fruitspunchs.productivityup.ui.deadlines.DeadlinesActivity;
import io.github.fruitspunchs.productivityup.ui.deadlines.DeadlinesActivityFragment;

public class MainActivity extends AppCompatActivity {
    public static final String LAST_INTERSTITIAL_DISPLAY_DATE_KEY = "LAST_INTERSTITIAL_DISPLAY_DATE_KEY";
    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.overview_card_deadlines)
    CardView mDeadlinesCardView;
    @Bind(R.id.overview_card_agenda)
    CardView mAgendaCardView;
    @Bind(R.id.overview_card_accountability_chart)
    CardView mAccountabilityChartCardView;
    @Bind(R.id.deadlines_task_recycler_view)
    RecyclerView mDeadlinesTaskRecyclerView;
    @Bind(R.id.deadlines_time_left_text_view)
    TextView mDeadlinesTimeLeftTextView;
    @Bind(R.id.deadlines_card_container)
    LinearLayout mDeadlinesCardContainer;
    @Bind(R.id.deadlines_no_item)
    TextView mDeadlinesNoItemTextView;
    @Bind(R.id.ultradian_rhythm_status_text_view)
    TextView mUltradianRhythmTimerTextView;
    @Bind(R.id.pomodoro_timer_timer_text_view)
    TextView mPomodoroTimerTextView;
    @Bind(R.id.pomodoro_timer_start_pause_button)
    ImageButton mPomodoroTimerStartPauseImageButton;
    @Bind(R.id.recycler_view)
    RecyclerView mAgendaCardRecyclerView;
    @Bind(R.id.agenda_no_item_text_view)
    TextView mAgendaNoItemTextView;
    @Bind(R.id.accountability_card_recycler_view)
    RecyclerView mAccountabilityCardRecyclerView;
    @Bind(R.id.accountability_no_item_text_view)
    TextView mAccountabilityNoItemTextView;

    private SharedPreferences mSharedPreferences;
    private PomodoroTimerCard mPomodoroTimerCard;
    private DeadlinesCard mDeadlinesCard;
    private AgendaCard mAgendaCard;
    private AccountabilityCard mAccountabilityCard;
    private UltradianRhythmTimerCard mUltradianRhythmTimerCard;
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;

    @OnClick(R.id.overview_card_deadlines)
    void clickDeadlinesCard() {
        Intent intent = new Intent(this, DeadlinesActivity.class);
        intent.setAction(DeadlinesActivityFragment.ACTION_SCROLL_TO_NEAREST_DAY);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_agenda)
    void clickAgendaCard() {
        Intent intent = new Intent(this, AgendaActivity.class);
        intent.setAction(AgendaActivityFragment.ACTION_SCROLL_TO_NEAREST_DAY);
        startActivity(intent);
    }

    @OnClick(R.id.overview_card_accountability_chart)
    void clickAccountabilityChartCard() {
        Intent intent = new Intent(this, AccountabilityActivity.class);
        intent.setAction(AccountabilityActivityFragment.ACTION_SCROLL_TO_NEAREST_DAY);
        startActivity(intent);
    }

    @OnFocusChange(R.id.overview_card_deadlines)
    void onDeadlinesCardFocused(boolean focused) {
        int color = this.getResources().getColor(R.color.cardViewBackgroundColor);
        if (focused) {
            color = this.getResources().getColor(R.color.lightGray);
        }
        mDeadlinesCardView.setCardBackgroundColor(color);
    }

    @OnFocusChange(R.id.overview_card_agenda)
    void onAgendaCardFocused(boolean focused) {
        int color = this.getResources().getColor(R.color.cardViewBackgroundColor);
        if (focused) {
            color = this.getResources().getColor(R.color.lightGray);
        }
        mAgendaCardView.setCardBackgroundColor(color);
    }

    @OnFocusChange(R.id.overview_card_accountability_chart)
    void onAccountabilityCardFocused(boolean focused) {
        int color = this.getResources().getColor(R.color.cardViewBackgroundColor);
        if (focused) {
            color = this.getResources().getColor(R.color.lightGray);
        }
        mAccountabilityChartCardView.setCardBackgroundColor(color);
    }

    @OnFocusChange(R.id.recycler_view)
    void onAgendaListFocus(boolean focused) {
        if (focused) {
            List<View> views = mAgendaCardRecyclerView.getFocusables(View.FOCUS_DOWN);
            if (views == null) {
                return;
            }
            if (views.size() > 0) {
                views.get(0).requestFocus(View.FOCUS_DOWN);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("A03F6ED8846D713161EA57CA533247A2")
                .build();

        mInterstitialAd.loadAd(adRequest);


        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.setAction(TimerService.ACTION_START_SERVICE);
        this.startService(serviceIntent);

        mDeadlinesCard = new DeadlinesCard(this, getSupportLoaderManager(), mDeadlinesTaskRecyclerView, mDeadlinesTimeLeftTextView, mDeadlinesNoItemTextView);
        mDeadlinesCard.onCreate();

        mUltradianRhythmTimerCard = new UltradianRhythmTimerCard(this, mUltradianRhythmTimerTextView);

        mPomodoroTimerCard = new PomodoroTimerCard(this, mPomodoroTimerStartPauseImageButton, mPomodoroTimerTextView);

        mAgendaCard = new AgendaCard(this, getSupportLoaderManager(), mAgendaCardRecyclerView, mAgendaNoItemTextView);
        mAgendaCard.onCreate();

        mAccountabilityCard = new AccountabilityCard(this, getSupportLoaderManager(), mAccountabilityCardRecyclerView, mAccountabilityNoItemTextView);
        mAccountabilityCard.onCreate();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type) && intent.hasExtra(Intent.EXTRA_TEXT)) {
                Intent agendaIntent = new Intent(this, AgendaActivity.class);
                agendaIntent.putExtra(AgendaActivity.BATCH_KEY, intent.getStringExtra(Intent.EXTRA_TEXT));
                agendaIntent.setAction(AgendaActivity.ACTION_ADD_BATCH);
                startActivity(agendaIntent);
            }
        }

        mPomodoroTimerStartPauseImageButton.requestFocus();
    }

    private void initializeAndDisplayInterstitial() {
        final long SECONDS_IN_DAY = 86400;
        long lastDisplayDate = mSharedPreferences.getLong(LAST_INTERSTITIAL_DISPLAY_DATE_KEY, 0);
        long currentTimeInSeconds = Utility.getCurrentTimeInSeconds();
        if (currentTimeInSeconds >= lastDisplayDate + SECONDS_IN_DAY || currentTimeInSeconds <= lastDisplayDate) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                mSharedPreferences.edit().putLong(LAST_INTERSTITIAL_DISPLAY_DATE_KEY, currentTimeInSeconds).apply();
            }
        }
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPomodoroTimerCard.onPause();
        mUltradianRhythmTimerCard.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPomodoroTimerCard.onResume();
        mUltradianRhythmTimerCard.onResume();
        initializeAndDisplayInterstitial();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int horizontalPadding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);

        View container = findViewById(R.id.container);
        if (container != null) {
            container.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        }
    }

    public interface CURSOR_LOADER_ID {
        int DEADLINE_TASKS = 0;
        int NEXT_DEADLINE = 1;
        int AGENDA = 2;
        int ACCOUNTABILITY = 3;
    }
}
