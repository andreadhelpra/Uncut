package com.andrea.uncut;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.andrea.uncut.Fragment.NotificationsFragment;
import com.andrea.uncut.Fragment.HomeFragment;
import com.andrea.uncut.Fragment.ProfileFragment;
import com.andrea.uncut.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView; // The bottom navigation menu
    Fragment selectedFragment = null; // Initialise the selected Fragment to null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);

        // Dynamically change the selected fragment
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // Show profile of the user selected from search activity
        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("publisherid"); // Get id of the user

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit(); // File can only be accessed by calling application
            editor.putString("id", publisher);
            editor.apply(); // Set the id of the profile to show

            // Show profile
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else { // If the intent is null display Home
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    // Handle what page to display based on item selected
private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        selectedFragment= new HomeFragment(); // Show Home
                        break;
                    case R.id.navigation_search:
                        selectedFragment= new SearchFragment(); // Show Search
                        break;
                    case R.id.navigation_add:
                        selectedFragment= null; // Display no fragment
                        item.setChecked(false); // Uncheck item
                        startActivity(new Intent(MainActivity.this, PostActivity.class)); // Start Post Activity where to upload new posts
                        break;
                    case R.id.navigation_notifications:
                        selectedFragment= new NotificationsFragment(); // Show Notifications
                        break;
                    case R.id.navigation_profile:
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit(); // File can only be accessed by calling application
                        editor.putString("id", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Display profile of the current user
                        editor.apply();
                        selectedFragment= new ProfileFragment(); // Show Profile
                        break;
                }
                if (selectedFragment!=null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit(); // Show the fragment
                }
                return true;
            }
        };
}