package com.andrea.uncut.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Adapter.SearchReviewAdapter;
import com.andrea.uncut.Model.Post;
import com.andrea.uncut.R;
import com.andrea.uncut.Adapter.UserAdapter;
import com.andrea.uncut.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView; // Recycler
    private UserAdapter userAdapter; // Adapter of users
    private List<User> mUsers; // List of users
    private SearchReviewAdapter searchReviewAdapter; // Adapter of reviews
    private List<Post> mPosts; // List of reviews
    private Button reviewsBtn; // Button to search reviews
    private Button usersBtn; // Button to search users
    EditText searchtxt; // Search input text

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView=view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Linear Layout

        searchtxt= view.findViewById(R.id.searchtxt);
        usersBtn= view.findViewById(R.id.users_btn);
        reviewsBtn= view.findViewById(R.id.reviews_btn);

        mUsers = new ArrayList<>(); // List of users
        userAdapter = new UserAdapter(getContext(), mUsers, true); // Initialise the user adapter

        recyclerView.setAdapter(userAdapter); // Set the User adapter as default adapter

        searchtxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // When text changes
                searchUsers(charSequence.toString().toLowerCase()); // Search user all in lowercase
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        readUsers(); // Read users

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPosts = new ArrayList<>();
                searchReviewAdapter = new SearchReviewAdapter(getContext(), mPosts, true);

                readReviews();

                recyclerView.setAdapter(searchReviewAdapter); // Set adapter to reviews
                searchtxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        searchReviews(charSequence.toString()); // Display reviews
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                reviewsBtn.setEnabled(false); // Disable the reviews button
                usersBtn.setEnabled(true); // Enable the users button
            }
        });

        usersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsers = new ArrayList<>();
                userAdapter = new UserAdapter(getContext(), mUsers, true); // Display reviews before text is changed

                recyclerView.setAdapter(userAdapter); // Set adapter to user adapter

                searchtxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        searchUsers(charSequence.toString().toLowerCase()); // Search Users
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                readUsers(); // Read all users
                usersBtn.setEnabled(false); // Disable the users button
                reviewsBtn.setEnabled(true); // Enable the reviews button
            }
        });


        return view;
    }

    private void searchUsers(String s){

        //Look for users in the database that correspond to the input text
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear(); // Clear list of users
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class); // Get single user
                    mUsers.add(user); // Add the user to the list
                }

                userAdapter.notifyDataSetChanged(); // Update the adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchReviews(String s){

        //Look for users in the database that correspond to the input text
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("title")
                .startAt(s)
                .endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear(); // Clear list of users
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);  // Get single post
                    mPosts.add(post); // Add the post to the list
                }

                searchReviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUsers() {

        // Location of the users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (searchtxt.getText().toString().equals("")){
                    mUsers.clear(); // Clear the list of users
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class); // Get value of user
                        mUsers.add(user); // Add user to the list
                    }

                    userAdapter.notifyDataSetChanged(); // Update adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readReviews() {

        // Location of the posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (searchtxt.getText().toString().equals("")){
                    mPosts.clear(); // Clear the list of posts
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Post post = snapshot.getValue(Post.class);  // Get value of post
                        mPosts.add(post); // Add post to the list
                    }

                    userAdapter.notifyDataSetChanged(); // Update adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}