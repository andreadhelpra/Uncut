package com.andrea.uncut.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Adapter.PostAdapter;
import com.andrea.uncut.Model.Post;
import com.andrea.uncut.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PostFragment extends Fragment {

    String postID; // Id of the post
    private RecyclerView recyclerView; // Recycler
    private PostAdapter postAdapter; // Adapter
    private List<Post> postList; // List of posts

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        SharedPreferences preference = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE); // Set preferences to private
        postID = preference.getString("postID", "none"); // Get post id

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()); // Linear Layout
        recyclerView.setLayoutManager(linearLayoutManager); // Set linear layout manager to recycler view

        postList = new ArrayList<>(); // List of posts
        postAdapter = new PostAdapter(getContext(), postList); // Post adapter
        recyclerView.setAdapter(postAdapter); // Set adapter to its recycler

        readPosts(); // Read all posts

        return view;
    }

    private void readPosts() {
        //Location of the post in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear(); // Clear the list of posts
                Post post = dataSnapshot.getValue(Post.class); // Get a single post
                postList.add(post); // Add the post to the list

                postAdapter.notifyDataSetChanged(); // Update the adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}