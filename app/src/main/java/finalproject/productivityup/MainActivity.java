package finalproject.productivityup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

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
        }

        return super.onOptionsItemSelected(item);
    }
}
