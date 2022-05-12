package com.asaadna.photogram.adapters;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asaadna.photogram.R;
import com.asaadna.photogram.models.User;
import com.asaadna.photogram.other.utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ViewHolder> {

    private Context context;
    private List<User> users;

    public PendingAdapter(Context context , List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_pending,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.pending_username.setText(user.username);

        holder.pending_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Remove from pending  -> signedIN
                //Insert into followes -> signedIN
                //Insert the user into Follwing -> holderUser
                //Remopve user from requests -> SignedIN

                FirebaseFirestore fireDB = FirebaseFirestore.getInstance();
                fireDB.collection("users").document(FirebaseAuth.getInstance()
                .getCurrentUser().getUid().toString()).update("pending", FieldValue.arrayRemove(user.id))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Pending","Removed " + user.username + " From pending");

                                fireDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser()
                                .getUid().toString()).update("followers",FieldValue.arrayUnion(user.id))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("Pending","Added " + user.username + " To Followers");

                                                fireDB.collection("users").document(user.id).update("following",FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Log.d("Pending" , "Added to following for holder " + user.username);

                                                                fireDB.collection("users").document(user.id)
                                                                        .update("requests",FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()))
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                Log.d("Pending","Remove from requests " + user.username);
                                                                                utils.toastDebug(context,"Request Accepted of " + user.username);
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });

            }
        });

        holder.pending_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                        .update("pending",FieldValue.arrayRemove("pending",user.id)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firestoreDB.collection("users").document(user.id).update("requests",FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        utils.toastDebug(view.getContext(), "Reject " + user.username);
                                    }
                                });
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView pending_username;
        private Button pending_accept , pending_reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pending_username = itemView.findViewById(R.id.pending_username);
            pending_accept = itemView.findViewById(R.id.pending_accept);
            pending_reject= itemView.findViewById(R.id.pending_reject);
        }
    }

}
