package edu.uw.notsetdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by joelross on 4/19/16.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //action bar "back"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //the FM is the guy who moves fragments around
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
