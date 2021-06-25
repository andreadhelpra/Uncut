package com.andrea.uncut.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Fragment.PostFragment;
import com.andrea.uncut.R;
import com.andrea.uncut.Model.Post;
import com.bumptech.glide.Glide;

import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.ViewHolder>{

    private Context context; // Context of the Adapter
    private List<Post> mPosts; // List of posts

    //Constructor
    public MyReviewAdapter(Context context, List<Post> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    // Inflate the Adapter
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reviews_item, parent, false);
        return new MyReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPosts.get(position); // Get a single post
        Glide.with(context).load(post.getPostImage()).into(holder.post_image); // Load its image
        holder.ratingBar.setRating(post.getScore()); // Set its rating
        holder.ratingBar.setIsIndicator(true); // Rating cannot be changed by the current user

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); //Editor to modify data
                editor.putString("postID", post.getPostid()); // Get the post id
                editor.apply(); // apply the editor

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostFragment()).commit(); // Start the Post Fragment
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView post_image; // Image of the post
        public RatingBar ratingBar; // Rating of the post

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

    }
}
