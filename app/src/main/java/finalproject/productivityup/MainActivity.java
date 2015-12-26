package finalproject.productivityup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
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
    List<TextView> cardTitles;
    boolean isShowingCardTitles = true;

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
            if (isShowingCardTitles) {
                Log.d(LOG_TAG, "Hiding card titles");
                for (TextView textView : cardTitles) {
                    textView.setVisibility(View.GONE);
                }
                item.setIcon(R.drawable.ic_show_card_title);
                isShowingCardTitles = false;
            } else {
                Log.d(LOG_TAG, "Showing card titles");
                for (TextView textView : cardTitles) {
                    textView.setVisibility(View.VISIBLE);
                }
                item.setIcon(R.drawable.ic_hide_card_title);
                isShowingCardTitles = true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
