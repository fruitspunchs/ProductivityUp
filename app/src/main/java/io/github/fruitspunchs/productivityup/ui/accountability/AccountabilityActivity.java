/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.ui.accountability;

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

public class AccountabilityActivity extends AppCompatActivity {

    private final String LOG_TAG = this.getClass().getSimpleName();
    @Bind(R.id.add_fab)
    FloatingActionButton mAddFab;

    @OnClick(R.id.add_fab)
    void clickAddFab() {
        Intent intent = new Intent(this, AddAccountabilityActivity.class);
        startActivityForResult(intent, 0);
    }

    public void onViewAttachedToWindow(long unixDate) {
        AccountabilityActivityFragment fragment = (AccountabilityActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.onViewAttachedToWindow(unixDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountability);
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
        AccountabilityActivityFragment fragment = (AccountabilityActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.onResult(requestCode, resultCode, data);
    }

}
