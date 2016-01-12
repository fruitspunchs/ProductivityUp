package finalproject.productivityup.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import finalproject.productivityup.R;

public class AgendaActivity extends AppCompatActivity {

    @Bind(R.id.add_agenda_fab)
    FloatingActionButton mAddFab;

    @OnClick(R.id.add_agenda_fab)
    void clickAddFab() {
        Intent intent = new Intent(this, AddAgendaActivity.class);
        startActivityForResult(intent, 0);
    }

    public void scrollToPosition(int position) {
        AgendaActivityFragment fragment = (AgendaActivityFragment) getSupportFragmentManager().findFragmentById(R.id.agenda_fragment);
        fragment.scrollToPosition(position);
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

}
