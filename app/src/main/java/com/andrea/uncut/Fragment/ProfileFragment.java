package com.andrea.uncut.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.EditProfileActivity;
import com.andrea.uncut.FollowersActivity;
import com.andrea.uncut.OptionsActivity;
import com.andrea.uncut.R;
import com.andrea.uncut.Adapter.MyReviewAdapter;
import com.andrea.uncut.Model.Post;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    ImageView image_profile; // Profile image
    ImageView options; // Options button
    TextView posts; // Number of Posts
    TextView followers; // Number of Followers
    TextView following; // Number of Followed users
    TextView fullname; // Full name
    TextView bio; // Bio
    TextView username; // Username
    Button edit_profile; // Edit profile button

    private List<String> mySaves; // List of saved items

    RecyclerView recyclerView_saves; // Recycler for saved posts
    MyReviewAdapter myWatchlistAdapter; // Adapter of saved posts
    List<Post> postList_saves; // List of saves

    RecyclerView recyclerView; // Recycler for reviews
    MyReviewAdapter myReviewAdapter; // Adapter of reviews
    List<Post> postList; // List of posts

    private FirebaseUser firebaseUser; // Firebase user

    String profileid; // Profile ID

    ImageButton myReviews; // Reviews button
    ImageButton watchlist; // Watchlist button

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the View
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get current user

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("id","none"); // Profile id

        // Set every variable to its UI component
        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);
        myReviews = view.findViewById(R.id.my_reviews);
        watchlist = view.findViewById(R.id.watchlist);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // Children of the recycler have fixed width and height

        // Linear Layout with 3 items
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();  // List of posts
        myReviewAdapter = new MyReviewAdapter(getContext(), postList); // Initialise the post adapter
        recyclerView.setAdapter(myReviewAdapter); // Set the Post adapter as default adapter

        recyclerView_saves = view.findViewById(R.id.recycler_view_watchlist);
        recyclerView_saves.setHasFixedSize(true); // Children of the recycler have fixed width and height

        // Linear Layout with 3 items
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>(); // List of saved posts
        myWatchlistAdapter = new MyReviewAdapter(getContext(), postList_saves); // Initialise the post adapter
        recyclerView_saves.setAdapter(myWatchlistAdapter); // Set the Post adapter as default adapter

        recyclerView.setVisibility(View.VISIBLE); // Default recycler view is reviews
        recyclerView_saves.setVisibility(View.GONE);

        userInfo(); // Get information about the user
        getFollowers(); // Get number of followers
        getNrPosts(); // Get number of posts
        myReviews(); // Get reviews
        myWatchlist(); // Get watchlist

        if (profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Edit Profile"); // If the profile is that of the current user display "Edit Profile" button
        } else {
            checkFollow(); // Otherwise a Follow/Unfollow button
            watchlist.setVisibility(View.GONE); // The visibility of the watchlist button is gone
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString(); // Get button text

                if (btn.equals("Edit Profile")){ // If it is "Edit Profile" show edit profile activity
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (btn.equals("follow")){ // If it is "Follow" add to the database, change text and send notifications
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotifications();
                } else if (btn.equals("following")){ // If it is "Following" remove to the database and change text
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        // If Reviews button is pressed show only reviews
        myReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        // If options buttons is pressed start Options Activity
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });

        // If Watchlist button is pressed show only saved posts
        watchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        // If followers is pressed start followers activity with users that follow current user
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        // If followers is pressed start followers activity with followed users
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        return view;
    }

    // Send notifications
    private void addNotifications(){
        //Get location of notification
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid()); // Add the user id
        hashMap.put("text", "started following you"); // Add text
        hashMap.put("postid", ""); // No post id
        hashMap.put("ispost", false); // Notification does not concern a post

        reference.push().setValue(hashMap); // Push to the database
    }

    // Get information from the user
    private void userInfo(){
        // Location of the user in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null){
                    return;
                }

                User user = snapshot.getValue(User.class); // Get single user

                Log.d("USER", String.valueOf(user)); // Checking user details are correct
                Glide.with(getContext()).load(user.getImageurl()).into(image_profile); // Get profile image
                username.setText(user.getUsername()); // Get username
                fullname.setText(user.getFullname()); // Get full name
                bio.setText(user.getBio()); // Get bio
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Check if user is being followed
    private void checkFollow(){
        // Location of the follow
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()){ // If present in the database
                    edit_profile.setText("following"); // Set text to "following"
                } else {
                    edit_profile.setText("follow"); // Otherwise set text to "follow"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Get the number of followers
    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(""+snapshot.getChildrenCount()); // Count number of followers and set it to text
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount()); // Count number of followers and set it to text
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Get number of posts
    private void getNrPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class); // Get single post
                    if (post.getPublisher().equals(profileid)){
                        i++; // Add one to the counter for each post
                    }
                }

                posts.setText(""+i); // Set text to post count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Show published reviews
    private void myReviews() {
        // Location of the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               postList.clear();
               for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                   Post post = snapshot.getValue(Post.class); // Get single post value
                   if (post.getPublisher().equals(profileid)){
                       postList.add(post); // Add post list
                   }
               }
                Collections.reverse(postList); // Show most recent first
                myReviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Show watchlist
    private void myWatchlist() {
        mySaves = new ArrayList<>(); // Initialise saved posts array

        // Location of the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mySaves.add(snapshot.getKey()); // Get key of the saved post
                }

                readSaves(); // Get saved items
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Read saved posts
    private void readSaves(){
        // Get location of the saved posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList_saves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class); // Get single post

                    for (String id:mySaves){
                        if (post.getPostid().equals(id)){ // If the post is saved
                            postList_saves.add(post); // Add post to the list
                        }
                    }
                }
                myWatchlistAdapter.notifyDataSetChanged(); // Update adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}