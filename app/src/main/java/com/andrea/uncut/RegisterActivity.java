package com.andrea.uncut;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends Activity {
    // The UI objects
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputUsername;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog; // A dialog showing progress indicator

    // Firebase relational database instances
    private FirebaseAuth mAuth; // Entry point of the Firebase Authentication SDK
    private DatabaseReference reference; // Location of user in the database

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputUsername = (EditText) findViewById(R.id.username);
        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        mAuth = FirebaseAuth.getInstance(); // Obtain an instance of FirebaseAuth

        // Link to Login Activity
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i); // Start the log in activity
                finish(); // Finish this activity
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false); // The dialog is cancelable with the BACK key

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Convert text to string and trim any additional blank space
                String username = inputUsername.getText().toString().trim();
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // None of the input fields can be empty and password must have minimum 6 characters
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(name) ||
                        TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }else if (password.length()<6){
                    Toast.makeText(getApplicationContext(), "Password must have at least 6 characters", Toast.LENGTH_SHORT).show();
                } else{
                    // Set the message of the dialog and show it
                    pDialog.setMessage("Please wait..");
                    pDialog.show();
                    register(username, name, email, password); // Push details to the database
                }
            }
        });
    }

    // Populate the firebase database with the new user details
    public void register(String strUsername, String strFullname, String strEmail, String strPassword ){
        // Create a user with email and password and handle the event when this is completed
        mAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = mAuth.getCurrentUser(); // Obtain the user
                            String userID = firebaseUser.getUid(); // Generate a unique user ID
                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID); // Build path of userID
                            // Create a hash map with the details of the user that will be stored in the database
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userID);
                            hashMap.put("username", strUsername.toLowerCase()); //Username always in lowercase
                            hashMap.put("fullname", strFullname);
                            hashMap.put("bio", "");
                            // Set default profile image
                            hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/uncut-301df.appspot.com/o/placeholder.png?alt=media&token=a1f6b379-1eb1-419c-aecd-80303454f5c7");

                            // Push the hashMap to the database
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    pDialog.dismiss(); // Dismiss the progress dialog
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    // Clear any task before activity is started and set MainActivity as the root of any task
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        } else { // Handle invalid email or password
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "email or password are invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}