package com.andrea.uncut;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrea.uncut.Adapter.CommentAdapter;
import com.andrea.uncut.Model.Comment;
import com.andrea.uncut.Model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView; // Recycler view for comments
    private CommentAdapter commentAdapter; // Adapter for comments
    private List<Comment> commentList; // List of comments

    EditText addcomment; // Input text for comment
    ImageView imageProfile; // Profile image of the comment publisher
    TextView post; // POST button

    String postid; // ID of the post
    String publisherid; // ID of the publisher

    FirebaseUser firebaseUser; // Firebase user


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set support for the toolbar
        getSupportActionBar().setTitle("Comments"); // Set title of the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Create BACK arrow
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            } // Terminate this activity
        });

        Intent intent = getIntent(); // Get intent from Main Activity
        postid = intent.getStringExtra("postid"); // Get post ID from Main Activity
        publisherid = intent.getStringExtra("publisherid"); // Get publisher ID from Main Activity

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); // Linear Layout
        recyclerView.setLayoutManager(linearLayoutManager); // Set linear layout manager to recycler view
        commentList = new ArrayList<>();  // List of comments
        commentAdapter = new CommentAdapter(this, commentList, postid); // Comment adapter
        recyclerView.setAdapter(commentAdapter); // Set the adapter of the recycler view to comment adapter

        // Assign variables to their UI components
        addcomment = findViewById(R.id.add_comment);
        imageProfile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        // Get instance of the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Handle POST button
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")){ // If text is empty throw error
                    Toast.makeText(CommentsActivity.this, "You need to write a comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment(); // Otherwise add the comment
                }
            }
        });

        getImage(); // Get the profile image of the publisher
        readComments(); // Read all comments
    }

    private void addComment(){
        // Get reference of the post's comments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        String commentid = reference.push().getKey(); // ID of the comment

        HashMap<String,Object> hashMap = new HashMap<>(); // Hash map with data of the comment
        hashMap.put("comment", addcomment.getText().toString()); // Text of the comment
        hashMap.put("publisher", firebaseUser.getUid()); // Publisher of the comment
        hashMap.put("commentid", commentid); // Id of the comment

        reference.child(commentid).setValue(hashMap); // Push data to the db
        addNotifications(); // Send notification to the post's publisher that current user commented
        addcomment.setText(""); // Reset text of the add comment input
    }

    private void addNotifications(){
        // Get reference of the publisher of the notification
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

        HashMap<String, Object> hashMap = new HashMap<>(); // Hash map with data of the notification
        hashMap.put("userid", firebaseUser.getUid()); // id of the user to send the notification to
        hashMap.put("text", "commented: " + addcomment.getText().toString()); // Text of the notification
        hashMap.put("postid", postid); // Id of the post
        hashMap.put("ispost", true); // Specify it comes from a post so when clicked it redirects to the post

        reference.push().setValue(hashMap); // Push the map to the db
    }

    private void getImage(){
        // Get reference of the user id in the db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class); // Get value of the user
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(imageProfile); // Uplooad the profile image to its container
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readComments(){
        // Get location of the comments for the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear(); // Clear all the comments previously uploaded
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class); // Get comment value
                    commentList.add(comment); // Add the comment to the list
                }

                commentAdapter.notifyDataSetChanged(); // Update the adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}