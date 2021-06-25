package com.andrea.uncut;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Adapter.UserAdapter;
import com.andrea.uncut.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    String id; // ID of the current user
    String title; // Activity title

    private List<String> idList; // List of IDs of Users

    RecyclerView recyclerView; // Users container
    UserAdapter userAdapter; // Users adapter
    List<User> userList; // List of followers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent(); // Get intent from Main Activity
        id = intent.getStringExtra("id"); // Get ID of the current user
        title = intent.getStringExtra("title"); // Get title of the activity (either 'likes', 'followers' or 'following')

        Toolbar toolbar = findViewById(R.id.toolbar); // Toolbar where to place the title
        setSupportActionBar(toolbar); // Set up the app bar
        getSupportActionBar().setTitle(title); // Set the title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Terminate this activity when BACK is pressed
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set Layout to 'Linear'
        userList = new ArrayList<>(); // Initialise list of users
        userAdapter = new UserAdapter(this, userList, false); // Set user adapter
        recyclerView.setAdapter(userAdapter); // Fill container with adapter

        idList = new ArrayList<>(); // List of users IDs.


        switch (title) {
            case "likes":
                getLikes(); // Get list of users that liked a post
                break;
            case "following":
                getFollowing(); // Get list of users that current user follows
                break;
            case "followers":
                getFollowers(); // Get list of users that follow the current user
                break;
        }

    }

    // Get list of users that follow the current user
    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("followers"); // Get followers from the database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear(); // Clear the list of ids
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey()); // Repopulate the list with followers
                }
                showUsers();  // Display the followers
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Get list of users that current user follows
    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("following"); // Get following users from the database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear(); // Clear the list of ids
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());  // Repopulate the list with following users
                }
                showUsers(); // Display the following users
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Get list of users that liked a post
    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(id);  // Get users that liked the post from the database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear(); // Clear the list of ids
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey()); // Repopulate the list with users that liked the post
                }
                showUsers(); // Display the  users that liked the post
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users"); // Get reference to users in db
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear(); // Clear the list of users
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class); // Get id of each user
                    for (String id : idList){
                        if (user.getId().equals(id)){ // If the id is present on the list of ids
                            userList.add(user); // Add the user to the list of users that will be displayed.
                        }
                    }
                }
                userAdapter.notifyDataSetChanged(); // notify that the data set changed and update
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}