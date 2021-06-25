package com.andrea.uncut.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.MainActivity;
import com.andrea.uncut.R;
import com.andrea.uncut.Model.User;
import com.andrea.uncut.Fragment.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private final Context mContext; // Context of the adapter
    private final List<User> mUsers; // List of users
    private boolean isFragment;  // Boolean to check if request comes from a fragment

    private FirebaseUser firebaseUser;  // Firebase user

    // Constructor
    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }

    // Inflate the adapter
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();// Get the location of current user
        final User user = mUsers.get(position); // Get single user
        holder.btn_follow.setVisibility(View.VISIBLE); // Set button to visible for users different than the current user
        holder.username.setText(user.getUsername()); // Set the username text
        holder.fullname.setText(user.getFullname()); // Set the full name text
        Glide.with(mContext).load(user.getImageurl()).into(holder.image_profile); // Load profile image
        isFollowing(user.getId(), holder.btn_follow); // Handle when Follow button is pressed
        if (user.getId().equals(firebaseUser.getUid())){
            holder.btn_follow.setVisibility(View.GONE); // Set visibility of the follow button to gone for current user
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); // Editor to modify data
                    editor.putString("id", user.getId());  // Get the post ID
                    editor.apply();  // Apply changes

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit(); // Show profile of the selected user if request goes to fragment
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class); // Show profile of the user
                    intent.putExtra("publisherid", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.btn_follow.getText().toString().equals("follow")){ // When the follow button is pressed
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true); // Upload following to the database
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);// Upload follower to the database
                    addNotifications(user.getId()); // Send notifications
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue(); // Remove value from following users
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue(); // Remove value from followers
                }
            }
        });
    }

    // Send "User started following you" notification
    private void addNotifications(String userid){
        // Location of the Notification
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid()); // Get user id
        hashMap.put("text", "started following you"); // Set text
        hashMap.put("postid", ""); // No post ID
        hashMap.put("ispost", false); // Notification does not concern a post

        reference.push().setValue(hashMap); // Push to database
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username; // Username
        public TextView fullname; // Full name
        public CircleImageView image_profile; // Profile image
        public Button btn_follow; // Follow button

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    // Change text of the follow button when pressed
    private void isFollowing(String userid, Button button){
        // Get location of the following user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()){ // If user is followed set text to 'following'
                    button.setText("following");
                }else{
                    button.setText("follow"); // If user is not followed set text to 'follow'
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
