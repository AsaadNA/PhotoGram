package com.asaadna.photogram.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.asaadna.photogram.R;
import com.asaadna.photogram.models.User;
import com.asaadna.photogram.other.utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class SearchAdapter extends FirestoreRecyclerAdapter<User,SearchAdapter.ViewHolder> {

    private User signedInUser;

    public SearchAdapter(FirestoreRecyclerOptions<User> options) {

        super(options);

        //ADD A SNAPSHOT EVENT LISTENER TO THE CURRENT SIGNED IN USER
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value == null || error != null) {
                            Log.e("Search","Error Occured while Getting SiginUser",error);
                        } else {;
                            signedInUser = value.toObject(User.class);
                            notifyDataSetChanged(); //Notify the adapter here
                        }
                    }
                });
    }

    private boolean hasRequested(String username) {
        if(signedInUser != null) {
            for(String s : signedInUser.requests) {
                if(s.equals(username)) return true;
            }
        } return false;
    }

    private boolean isFollowing(String username) {
        if(signedInUser != null) {
            for(String s : signedInUser.following) {
                if(s.equals(username)) return true;
            }
        } return false;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User model) {
        holder.search_username.setText(model.username);

        /* REQUEST BUTTON */

        if(hasRequested(model.id)) {
            holder.search_send.setText("Cancel Request");
        } else if(isFollowing(model.id)){
            holder.search_send.setText("Unfollow");
        } else {
            holder.search_send.setText("Follow");
        }

        holder.search_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Handling Cases

                FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                if(!hasRequested(model.id) && !isFollowing(model.id)) {
                    firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                            .update("requests", FieldValue.arrayUnion(model.id)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Log.d("SEARCH" , "Request Sento " + model.id);

                            //Update the other users pending ...
                            FirebaseFirestore.getInstance().collection("users").document(model.id).update("pending",FieldValue.arrayUnion(signedInUser.id))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("SEARCH" , "Updated Pending");
                                        }
                                    });

                        }
                    });
                } else if(hasRequested(model.id)) {
                    firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                            .update("requests", FieldValue.arrayRemove(model.id)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Log.d("SEARCH" , "Cancelled Request For " + model.id);

                            //Update the other users pending ...
                            FirebaseFirestore.getInstance().collection("users").document(model.id).update("pending",FieldValue.arrayRemove(signedInUser.id))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("SEARCH" , "Updated Pending");
                                        }
                                    });

                        }
                    });
                } else if(isFollowing(model.id)) {
                    firestoreDB.collection("users").document(signedInUser.id).update("following" , FieldValue.arrayRemove(model.id))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    firestoreDB.collection("users").document(model.id).update("followers",FieldValue.arrayRemove(signedInUser.id))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    utils.toastDebug(view.getContext(), "Unfollowed " + model.username);
                                                }
                                            });
                                }
                            });
                }

            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_search,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView search_username;
        private Button search_send;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            search_username = itemView.findViewById(R.id.search_username);
            search_send = itemView.findViewById(R.id.search_button);
        }
    }

}
