package com.andrea.uncut;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    Button login; // The 'log in' button
    Button register; // The 'create account' button

    FirebaseUser firebaseUser; // The user that will be pushed to the firebase db

    // Phone starts after it has been created
    @Override
    protected void onStart(){
        super.onStart();

        // Get the user that is accessing the app from the db
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Redirect to the Main Activity if the user is already logged in
        if (firebaseUser!=null){
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish(); // Terminate the current activity.
        }
    }

    // Phone is initialised
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        login = findViewById(R.id.log_in);
        register = findViewById(R.id.create_account);

        // When 'log in' is pressed, redirect user to the Login Activity
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        // When 'create account' is pressed, redirect user to the Register Activity
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}