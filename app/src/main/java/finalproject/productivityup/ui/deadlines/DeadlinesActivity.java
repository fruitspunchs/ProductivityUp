package finalproject.productivityup.ui.deadlines;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import finalproject.productivityup.R;

public class DeadlinesActivity extends AppCompatActivity {

    @Bind(R.id.deadlines_add_fab)
    FloatingActionButton mAddFab;

    @OnClick(R.id.deadlines_add_fab)
    void clickAddFab() {
        Intent intent = new Intent(this, AddDeadlineActivity.class);
        startActivityForResult(intent, 0);
    }

    public void scrollToPosition(long position) {
        DeadlinesActivityFragment fragment = (DeadlinesActivityFragment) getSupportFragmentManager().findFragmentById(R.id.deadlines_fragment);
        fragment.scrollToPosition(position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadlines);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
    }

}
