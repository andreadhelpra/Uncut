package com.andrea.uncut.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.MainActivity;
import com.andrea.uncut.R;
import com.andrea.uncut.Model.Comment;
import com.andrea.uncut.Model.User;
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

import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private Context mContext; // Context opf the Adapter
    private List<Comment> mComment; // List of comments
    private String postid; // Post id

    private FirebaseUser firebaseUser; // Firebase user

    //Constructor
    public CommentAdapter(Context context, List<Comment> comments, String postid){
        mContext = context;
        mComment = comments;
        this.postid = postid;
    }

    //Inflate the Adapter
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get location of the current user
        Comment comment = mComment.get(position); //  Get a single comment

        holder.comment.setText(comment.getComment()); // Set the text of the comment to the one stored in Firebase
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher()); // Get user image and username

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher()); // Put the publisher id as extra
                mContext.startActivity(intent);  // Show the publisher profile
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher()); // Put the publisher id as extra
                mContext.startActivity(intent); // Show the publisher profile
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(comment.getPublisher().equals(firebaseUser.getUid())){ // If the publisher is the current user
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create(); // Start Alert Dialog
                    alertDialog.setTitle("Delete this comment?"); // Ask user if he/she wants to delete the comment
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // If No is selected dismiss the dialog
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If Yes is selected remove the comment from the database
                            FirebaseDatabase.getInstance().getReference("Comments").child(postid).child(comment.getCommentid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()){
                                      Toast.makeText(mContext, "Comment deleted", Toast.LENGTH_SHORT).show(); // Inform the user
                                  }
                                }
                            });
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });
                    alertDialog.show(); // Show the dialog
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile; // Profile Image
        public TextView username; // Username
        public TextView comment; // Comment

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            image_profile=itemView.findViewById(R.id.image_profile);
            username=itemView.findViewById(R.id.username);
            comment=itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(ImageView imageView, TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherid); // location of the user

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class); // Get value of the user
                Glide.with(mContext).load(user.getImageurl()).into(imageView); // Glide the image profile to its container
                username.setText(user.getUsername()); // Set username text
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
