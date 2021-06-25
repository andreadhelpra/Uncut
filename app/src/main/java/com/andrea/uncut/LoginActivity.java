package com.andrea.uncut;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText email; // The email input
    EditText password; // The password input
    Button login; // The log in button
    Button signup; // The link to the register activity

    FirebaseAuth mAuth;  // Entry point of the Firebase Authentication SDK

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.btnLinkToRegisterScreen);
        signup.setBackground(null); // Hide the background of the button so it appears as a text.
        mAuth = FirebaseAuth.getInstance();

        //If the link to register is pressed redirect to Register Activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

        //Handle log in button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create a progress dialog to show any message related to the log in progress
                ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);

                String str_email = email.getText().toString().trim(); // Get email input and trim
                String str_password = password.getText().toString().trim(); // Get password input and trim

                //Show toast when at least one field is empty
                if(TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                } else { // All input fields are filled
                    pDialog.setMessage("Please wait..");
                    pDialog.show(); // Show progress dialog

                    // Sign in the user using email and password and handle event once this is done
                    mAuth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){ // If the email and password are valid get the user ID as a reference
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(mAuth.getCurrentUser().getUid());

                                        // Handle reference
                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                pDialog.dismiss(); // Hide progress dialog
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent); // Start Main Activity
                                                finish(); // Terminate current session
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                              pDialog.dismiss(); // When user presses BACK key hide the progress dialog
                                            }
                                        });
                                    } else{ // If either the email is not valid or the password is wrong show error message
                                        pDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });
    }
}