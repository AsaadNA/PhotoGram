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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.asaadna.photogram.R;
import com.asaadna.photogram.models.Post;
import com.asaadna.photogram.models.User;
import com.asaadna.photogram.other.utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private LayoutInflater inflater;
    private User signedInUser;

    //Checks whether the postID is in the hearted Array of the user
    //This basically changes due to the event listener on the documentSnapshot
    private boolean isHearted(String postID) {
        if(signedInUser != null) {
            for (String s : signedInUser.hearts) {
                if (s.equals(postID)) return true;
            }
        } return false;
    }

    public PostsAdapter(Context context , List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.inflater = LayoutInflater.from(context);

        //ADD A SNAPSHOT EVENT LISTENER TO THE CURRENT SIGNED IN USER
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value == null || error != null) {
                            Log.e("Posts","Error Occured while Getting SiginUser",error);
                        } else {;
                            signedInUser = value.toObject(User.class);
                            notifyDataSetChanged(); //Notify the adapter here
                        }
                    }
                });
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
        holder.postUsername.setText(p.user.username);
        holder.description.setText(p.description);
        holder.timestamp.setText(DateUtils.getRelativeTimeSpanString(p.creation_time_ms));

        /** SETTING LIKES/HEART COUNTER **/

        //Setting a snapListener for realtime changes to the likes
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("posts").document(p.getID()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                int count = value.toObject(Post.class).hearts.size();
                if(count == 1) holder.likesTextView.setText(count + " like");
                else holder.likesTextView.setText(count + " likes");
            }
        });

        /*** SETTING HEART Button ****/

        //Setting up the hearts
        if(isHearted(p.getID())) {
            holder.heartButton.setImageResource(R.drawable.heartfilled);
        } else {
            holder.heartButton.setImageResource(R.drawable.heart);
        }

        //UPDATES THE HEARTS ARRAY OF BOTH USER AND THE POST
        holder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if already hearted remove from signin users hearts array
                FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                if(isHearted(p.getID())) {
                    firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser()
                            .getUid().toString()).update("hearts", FieldValue.arrayRemove(p.getID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //utils.toastDebug(context,"Unliked Post");
                            Log.d("POSTS","Unlike Successful " + p.getID());

                            //Also update the posts hearts array
                            firestoreDB.collection("posts").document(p.getID()).update("hearts",FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("POSTS" , "Updated POST ALSO ");
                                }
                            });
                        }
                    });

                } else {
                    firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser()
                            .getUid().toString()).update("hearts", FieldValue.arrayUnion(p.getID())).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //utils.toastDebug(context,"U liked ");
                            Log.d("POSTS","like Successful " + p.getID());

                            //Also update the posts hearts array
                            firestoreDB.collection("posts").document(p.getID()).update("hearts",FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser()
                                    .getUid().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("POSTS" , "Updated POST ALSO ");
                                }
                            });
                        }
                    });
                }
            }
        });

        //Loading POST IMAGE
        CircularProgressDrawable progress = new CircularProgressDrawable(context);
        progress.setColorSchemeColors(Color.GRAY, Color.GRAY, Color.GRAY);
        progress.setCenterRadius(40f);
        progress.setStrokeWidth(20f);
        progress.start();
        Picasso.get().load(p.image_url).placeholder(progress).into(holder.postImage);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username,postUsername,description,timestamp,likesTextView;
        private final ImageView postImage,heartButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            postUsername = itemView.findViewById(R.id.postUsername);
            description = itemView.findViewById(R.id.description);
            timestamp = itemView.findViewById(R.id.timestampTextView);
            postImage = itemView.findViewById(R.id.postImage);
            heartButton = itemView.findViewById(R.id.heartButton);
            likesTextView = itemView.findViewById(R.id.likesTextView);
        }
    }
}
