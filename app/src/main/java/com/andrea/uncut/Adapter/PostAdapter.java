package com.andrea.uncut.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.CommentsActivity;
import com.andrea.uncut.FollowersActivity;
import com.andrea.uncut.Fragment.ProfileFragment;
import com.andrea.uncut.Model.Post;
import com.andrea.uncut.Model.User;
import com.andrea.uncut.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
public Context mContext; // Context of the adapter
public List<Post> mPost; // List of posts

private FirebaseUser firebaseUser; // firebase user

    // Constructor
    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    // Inflate the adapter
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get location of the firebase author
        Post post = mPost.get(position); // Get single post
        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image); // Glide image of the post to the container
        holder.shortDescription.setText(post.getDescription()); // Show short description
        holder.description.setText(post.getDescription()); // Set text of the full description but hide for now
        holder.title.setText(post.getTitle()); // Set text of the title

        holder.ratingBar.setRating(post.getScore()); // Set rating of tthe post
        holder.ratingBar.setIsIndicator(true); // Rating cannot be changed

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher()); // Get image and username of the publisher
        isLiked(post.getPostid(), holder.like); // Handle like button
        nrLikes(holder.likes, post.getPostid()); // Get the number of likes
        getComments(post.getPostid(), holder.comments); // Get the comments of the post
        isSaved(post.getPostid(), holder.save); // Handle save button

        // Redirect to the profile of the publisher
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); // Editor to modify data
                editor.putString("id", post.getPublisher()); // ID of the publisher
                editor.apply(); // Apply changes

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit(); // Switch to the publisher profile
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); // Editor to modify data
                editor.putString("id", post.getPublisher());  // ID of the publisher
                editor.apply(); // Apply changes

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();  // Switch to the publisher profile
            }
        });

        holder.shortDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.shortDescription.setVisibility(View.GONE); // Hide the short description
                holder.description.setVisibility(View.VISIBLE); // Show the full description / review
            }
        });

        holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.description.setVisibility(View.GONE); // Hide the full description
                holder.shortDescription.setVisibility(View.VISIBLE); // Show the short description / review
            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); // Editor to modify data
                editor.putString("id", post.getPublisher()); // ID of the publisher
                editor.apply();  // Apply changes

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();  // Switch to the publisher profile
            }
        });

        holder.save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (holder.save.getTag().equals("save")){ // If save button is pressed
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true); // Save the post in the database
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue(); // Remove the saved post from the database
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")){ // If button is pressed
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true); // Add like to the database
                    addNotifications(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue(); // Remove like from the database
                }
            }
        });

        // Start Followers Activity but with people that liked the post instead
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostid()); // Get post id
                intent.putExtra("title", "likes"); // Get title and likes
                mContext.startActivity(intent);
            }
        });

        // Start Comments Activity of the post
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid()); // Get post ID
                intent.putExtra("publisherid", post.getPublisher()); // Get Publisher
                mContext.startActivity(intent);
            }
        });

        // Start Comments Activity of the post
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid()); // Get post ID
                intent.putExtra("publisherid", post.getPublisher()); // Get Publisher
                mContext.startActivity(intent);
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu =  new PopupMenu(mContext, v); // Initialise popup menu
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit_title:
                                editTitle(post.getPostid()); // Edit the title of the post
                                return true;
                            case R.id.edit_rating:
                                editRating(post.getPostid());  // Edit the rating of the post
                                return true;
                            case R.id.edit_description:
                                editDescription(post.getPostid());  // Edit the description of the post
                                return true;
                            case R.id.delete:
                                final String id = post.getPostid(); // Get the id of the post
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostid()).removeValue() // Remove it from the database
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    deleteNotifications(id, firebaseUser.getUid()); // Delete the notifications too
                                                }
                                            }
                                        });
                                return true;
                            case R.id.report: // Show a message when report is clicked
                                Toast.makeText(mContext, "Thank you for your collaboration, we will carefully review this item", Toast.LENGTH_LONG).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu); // Inflate the menu container
                if(!post.getPublisher().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.edit_title).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.edit_rating).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.edit_description).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show(); // Show the Popup menu
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile; // Profile image
        public ImageView post_image; // Post image
        public ImageView like; // Like button
        public ImageView comment; // Comment button
        public ImageView save; // Save button
        public ImageView more; // More button
        public TextView username; // Username of the publisher
        public TextView likes; // Number of likes
        public TextView publisher; // Username of the publisher
        public RatingBar ratingBar; // Rating bar
        public TextView title; // Title of the post
        public TextView shortDescription; // First line of the description
        public TextView description; // Full description
        public TextView comments; // Number of comments

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Assign each UI component to its variable
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            title = itemView.findViewById(R.id.title);
            shortDescription = itemView.findViewById(R.id.shortDescription);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);

        }
    }

    // Get the number of comments
    private void getComments(String postid, TextView comments){
        // Get the location of comments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText(String.valueOf(snapshot.getChildrenCount())); // Show number of comments
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked (String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get current user

        // Get location of likes
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()){ // When like button is pressed change its colour to red
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked"); // Change tag to 'liked'
                } else {
                    imageView.setImageResource(R.drawable.ic_like_white); // When like button is unpressed change its colour to white
                    imageView.setTag("like"); // Change tag to 'like'
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Add notifications when a post is liked
    private void addNotifications(String userid, String postid){
        // Location of the Notification
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid()); // id of the user receiving the like
        hashMap.put("text", "liked your post"); // Text of the notification
        hashMap.put("postid", postid); // ID of the post
        hashMap.put("ispost", true); // Notifications concerns a post

        reference.push().setValue(hashMap); // Push to database
    }

    // Delete notifications when a post gets deleted
    private void deleteNotifications(final String postid, String userid){
        // Location of the notifications to be deleted
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("postid").getValue().equals(postid)){ // Get the post id
                        snapshot.getRef().removeValue() // Remove value
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // Inform the user the post got deleted
                                        Toast.makeText(mContext, "Post deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Get the number of likes
    private void nrLikes(TextView likes, String postid){
        // Get the location of likes of with that post id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(String.valueOf(snapshot.getChildrenCount())); // Get their count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Get information of the publisher
    private void publisherInfo(ImageView image_profile, TextView username, TextView publisher, String userID){
        // Location of the user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class); // Values of the user
                Glide.with(mContext).load(user.getImageurl()).into(image_profile); // Laod the profile image
                username.setText(user.getUsername()); // Get the username
                publisher.setText(user.getUsername());  // Get the publisher name (username)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Save a post
    private void isSaved(String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get current user

        // Location of the saved posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()){ // When the post is in Saves in the database
                    imageView.setImageResource(R.drawable.ic_save_green); // Set the icon to green
                    imageView.setTag("saved"); // Set tag to "saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_save_white); // Set image to white when post is unsaved
                    imageView.setTag("save"); // Set tag to "save"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void editTitle(String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext); // Initialise alert dialog
        alertDialog.setTitle("Edit Title"); // Set title of the dialog to "Edit Title"

        EditText editText = new EditText(mContext); // New edit text
        // Set Linear Layout for dialog
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getTitleText(postid, editText); // Set Text to the current title

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() { // Positive button "Edit"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("title", editText.getText().toString()); // Get text from the editText and set it to the new title

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(hashMap); // Update the title
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // Negative button to "Cancel"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show(); // Show the dialog
    }

    private void editRating(String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext); // Initialise alert dialog
        alertDialog.setTitle("Edit Rating"); // Set title of the dialog to "Edit Rating"

        RatingBar ratingBar = new RatingBar(mContext); // New Rating Bar
        ratingBar.setNumStars(5); // Rating bar of 5 star
        ratingBar.setStepSize(1); // Step of 1 star

        // Set Linear Layout for dialog
        LinearLayout linearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        ratingBar.setLayoutParams(lParams);
        linearLayout.addView(ratingBar);

        alertDialog.setView(linearLayout);

        getRatingStars(postid, ratingBar); // Set rating of the edit rating bar to the current rating

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() { // Positive button "Edit"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("score", ratingBar.getRating()); // Get new rating

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(hashMap); // Update on the database
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  // Negative button to "Cancel"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show(); // Show the dialog
    }

    private void editDescription(String postid){ // Initialise alert dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext); // Initialise alert dialog
        alertDialog.setTitle("Edit Review"); // Set title of the dialog to "Edit Review"

        EditText editText = new EditText(mContext); // New edit text
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( // Set Linear Layout for dialog
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getDescriptionText(postid, editText); // Set Text to the current description

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() { // Positive button to "Edit"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", editText.getText().toString()); // Get new description

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(hashMap); // Update on database
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  //Negative button to "Cancel"
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show(); // Show the dialog
    }

    private void getTitleText(String postid, EditText editText){
        // Location od the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getTitle()); // Set Text to the current title
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRatingStars (String postid, RatingBar ratingBar){
        // Location od the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingBar.setNumStars(5); // 5 starts
                ratingBar.setStepSize(1); // Step every 1 star
                ratingBar.setRating(snapshot.getValue(Post.class).getScore()); // Set rating to the current rating
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDescriptionText(String postid, EditText editText){
        // Location od the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getDescription()); // Set description to the current one
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
