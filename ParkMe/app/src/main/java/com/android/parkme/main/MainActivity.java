package com.android.parkme.main;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.parkme.R;
import com.android.parkme.common.FindSlotsFragment;
import com.android.parkme.common.PersonalDetailsFragment;
import com.android.parkme.query.RaiseQueryFragment;
import com.android.parkme.query.ViewQueriesFragment;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView navBar;
    private String fragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navBar = findViewById(R.id.bottomNavigationView);
        navBar.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.profile:
                    Functions.setCurrentFragment(this, new PersonalDetailsFragment());
                    break;
                case R.id.home:
                    Functions.setCurrentFragment(this, new HomeFragment());
                    break;
                case R.id.announcement:
                    Functions.setCurrentFragment(this, new ViewQueriesFragment());
                    break;
                case R.id.query:
                    Functions.setCurrentFragment(this, new RaiseQueryFragment());
                    break;
                case R.id.findSlots:
                    Functions.setCurrentFragment(this, new FindSlotsFragment());
                    break;
            }
            return true;
        });

        Functions.setCurrentFragment(this, new HomeFragment());

        FirebaseMessaging.getInstance().subscribeToTopic(Globals.NOTIFICATION_TOPIC)
                .addOnCompleteListener(task -> {
                });
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.i(TAG, "" + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() == 1) {
            fragmentManager.popBackStack();
            finish();
        } else {
            Log.i(TAG, "super.onBackPressed()");
            super.onBackPressed();
        }
    }

}