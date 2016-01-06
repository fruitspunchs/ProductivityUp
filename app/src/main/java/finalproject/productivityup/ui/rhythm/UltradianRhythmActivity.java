package finalproject.productivityup.ui.rhythm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import finalproject.productivityup.R;

// TODO: 1/7/2016 use countdown timer 
// TODO: 1/7/2016 store time started 
// TODO: 1/7/2016 store if work or rest
public class UltradianRhythmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultradian_rhythm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
