package finalproject.productivityup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TimePicker;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDeadlineActivity extends AppCompatActivity {

    @Bind(R.id.add_deadline_done_fab)
    FloatingActionButton doneFab;

    @Bind(R.id.add_deadline_calendar_view)
    CalendarView calendarView;

    @Bind(R.id.add_deadline_time_picker)
    TimePicker timePicker;

    @Bind(R.id.add_deadline_date_time_image_button)
    ImageButton dateTimeButton;
    private int mode = MODE.DATE;

    @OnClick(R.id.add_deadline_done_fab)
    void clickDoneFab() {
        finish();
    }

    @OnClick(R.id.add_deadline_date_time_image_button)
    void clickDateTimeButton() {
        switch (mode) {
            case MODE.DATE:
                mode = MODE.TIME;
                dateTimeButton.setImageResource(R.drawable.ic_event_white_48dp);
                calendarView.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                break;
            case MODE.TIME:
                mode = MODE.DATE;
                dateTimeButton.setImageResource(R.drawable.ic_alarm_white_48dp);
                calendarView.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deadline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
    }

    private interface MODE {
        int DATE = 0;
        int TIME = 1;
    }

}
