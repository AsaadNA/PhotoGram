package com.asaadna.photogram.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.asaadna.photogram.R;
import com.asaadna.photogram.models.Post;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private LayoutInflater inflater;

    public PostsAdapter(Context context , List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.single_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = posts.get(position);
        holder.username.setText(p.user.username);
        holder.description.setText(p.description);
        holder.timestamp.setText(DateUtils.getRelativeTimeSpanString(p.creation_time_ms));

        // create a ProgressDrawable object which we will show as placeholder
        CircularProgressDrawable progress = new CircularProgressDrawable(context);
        progress.setColorSchemeColors(Color.CYAN, Color.BLACK, Color.RED);
        progress.setCenterRadius(40f);
        progress.setStrokeWidth(10f);
        progress.start();

        Picasso.get().load(p.image_url).placeholder(progress).into(holder.postImage);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username,description,timestamp;
        private final ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            description = itemView.findViewById(R.id.description);
            timestamp = itemView.findViewById(R.id.timestampTextView);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }
}