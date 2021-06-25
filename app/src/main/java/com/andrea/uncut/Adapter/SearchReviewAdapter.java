package com.andrea.uncut.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.andrea.uncut.Fragment.PostFragment;
import com.andrea.uncut.Model.Post;
import com.andrea.uncut.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class SearchReviewAdapter extends RecyclerView.Adapter<SearchReviewAdapter.ViewHolder> {

    private final Context mContext; // Context of the adapter
    private final List<Post> mPosts; // List of posts
    private boolean isFragment; // Boolean to check if request comes from a fragment

    private FirebaseUser firebaseUser; // Firebase user

    // Constructor
    public SearchReviewAdapter(Context mContext, List<Post> mPosts, boolean isFragment) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        this.isFragment = isFragment;
    }

    // Inflate the adapter
    @NonNull
    @Override
    public SearchReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_review_item, parent, false);
        return new SearchReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchReviewAdapter.ViewHolder holder, final int position) {

        final Post post = mPosts.get(position); // Get single post
        holder.title.setText(post.getTitle()); // Upload title to its container
        holder.review.setText(post.getDescription()); // Upload review to its container
        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image); // Upload image to its container
        holder.rating_bar.setRating(post.getScore()); // Upload rating to its container
        holder.rating_bar.setIsIndicator(true); // Rating cannot be changed

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit(); // Editor to modify data
                    editor.putString("postID", post.getPostid()); // Get the post ID
                    editor.apply(); // Apply changes

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostFragment()).commit(); // Show full post when an Item is selected
            }
        });
    }

    @Override
    public int getItemCount(){
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title; // Title of the post
        public TextView review; // Review text
        public ImageView post_image; // Image of the post
        public RatingBar rating_bar; // Rating bar

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            title = itemView.findViewById(R.id.title);
            review = itemView.findViewById(R.id.review);
            post_image = itemView.findViewById(R.id.post_image);
            rating_bar = itemView.findViewById(R.id.rating_bar);
        }
    }
}