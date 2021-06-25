package com.andrea.uncut;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class OptionsActivity extends AppCompatActivity {
    TextView settings; // The settings button
    TextView logout; // The logout button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Toolbar toolbar = findViewById(R.id.toolbar); // The toolbar for activity title and back arrow
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Options"); // Activity title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Up button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Terminate the current activity and return to Main Activity
            }
        });

        settings = findViewById(R.id.settings);
        logout = findViewById(R.id.logout);

        // Show toast when settings is clicked as it has not been implemented yet
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(OptionsActivity.this, "Not implemented yet.", Toast.LENGTH_SHORT).show();
            }
    });

        // Handle logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut(); // Sign out
                startActivity(new Intent(OptionsActivity.this, StartActivity.class) // Navigate to Start Activity
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                // Since StartActivity is already running, instead of launching a new instance of it, close all the activities on top of it
            }
        });
    }
}