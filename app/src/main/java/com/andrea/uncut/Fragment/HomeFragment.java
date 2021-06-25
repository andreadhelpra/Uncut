package com.andrea.uncut.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.R;
import com.andrea.uncut.Adapter.PostAdapter;
import com.andrea.uncut.Model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView; // Recycler
    private PostAdapter postAdapter; // Adapter
    private List<Post> postLists; // List of posts

    private List<String> followingList; // List of following users

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the recycler
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());  // Linear Layout
        linearLayoutManager.setReverseLayout(true);  // Set linear layout manager to recycler view
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>(); // List of posts
        postAdapter = new PostAdapter(getContext(), postLists); // Post adapter
        recyclerView.setAdapter(postAdapter); // Set adapter to its recycler
        checkFollowing(); // Read all following users

        return view;
    }

    private void checkFollowing(){
        // New following list
        followingList = new ArrayList<>();

        // Location of the following users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear(); // Clear the list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    followingList.add(dataSnapshot.getKey()); // Add followed users to the list
                }
                readPosts(); // Read the posts of that user
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts(){
        // Get location of the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear(); // Clear the list of posts
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class); // Get the value of a single post
                    for (String id : followingList){
                        if (post.getPublisher().equals(id)) {  // If the publisher of the post is followed by the current user
                            postLists.add(post); // Add the post to post list which will then display the post
                        }
                    }
                }

                postAdapter.notifyDataSetChanged(); // Update the adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}