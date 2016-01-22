package finalproject.productivityup.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import finalproject.productivityup.R;

public class AgendaActivity extends AppCompatActivity {
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        AgendaActivityFragment fragment = (AgendaActivityFragment) getSupportFragmentManager().findFragmentById(R.id.agenda_fragment);
        fragment.onResult(requestCode, resultCode, data);
    }

}
