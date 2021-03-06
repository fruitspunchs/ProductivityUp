/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.deadlines;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.fruitspunchs.productivityup.R;

public class DeadlinesActivity extends AppCompatActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.deadlines_add_fab)
    FloatingActionButton mAddFab;

    @OnClick(R.id.deadlines_add_fab)
    void clickAddFab() {
        Intent intent = new Intent(this, AddDeadlineActivity.class);
        startActivityForResult(intent, 0);
    }

    public void onViewAttachedToWindow(long unixDate) {
        DeadlinesActivityFragment fragment = (DeadlinesActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.onViewAttachedToWindow(unixDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadlines);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        DeadlinesActivityFragment fragment = (DeadlinesActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.onResult(requestCode, resultCode, data);
    }
}
