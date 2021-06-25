package com.andrea.uncut.Adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Fragment.PostFragment;
import com.andrea.uncut.R;
import com.andrea.uncut.Model.Notification;
import com.andrea.uncut.Model.Post;
import com.andrea.uncut.Model.User;
import com.andrea.uncut.Fragment.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private Context mContext; // Context
    private List<Notification> mNotification; // List of notifications

    // Constructor
    public NotificationsAdapter(Context context, List<Notification> notification){
        mContext = context;
        mNotification = notification;
    }

    // Inflate the Adapter
    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsAdapter.ViewHolder holder, final int position) {

        final Notification notification = mNotification.get(position); // Get a single Notification

        holder.text.setText(notification.getText()); // Set text of the notification

        getUserInfo(holder.image_profile, holder.username, notification.getUserid()); // Get profile image and username from user

        if (notification.isIspost()) {
            holder.post_image.setVisibility(View.VISIBLE); // Set the Post image to visible if notification concerns a post
            getPostImage(holder.post_image, notification.getPostid());// Get the post image
        } else {
            holder.post_image.setVisibility(View.GONE); // Post image invisible if notification does not conern a post
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isIspost()) { // If the notification concerns a post
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit(); // Editor to modify data
                    editor.putString("postID", notification.getPostid()); // Get the post ID
                    editor.apply(); // Apply the editor

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostFragment()).commit(); // Start Post Fragment
                } else { // If notification concerns a user
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit(); // Editor to modify data
                    editor.putString("id", notification.getUserid()); // Get the profile ID
                    editor.apply(); // Apply the editor

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit(); // Apply the editor
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile; // Profile Image
        public ImageView post_image; // Post image
        public TextView username; // Username
        public TextView text; // Text of the notification

        public ViewHolder(View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(publisherid); // Get location of the user

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // Get single user
                Glide.with(mContext).load(user.getImageurl()).into(imageView); // Glide the profiile image to the container
                username.setText(user.getUsername()); // Set text of the profile image
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostImage(final ImageView post_image, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Posts").child(postid); // Location of the profile

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class); // Get single post
                Glide.with(mContext).load(post.getPostImage()).into(post_image); // Glide the post image to the container
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}