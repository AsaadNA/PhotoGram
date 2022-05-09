package com.asaadna.photogram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asaadna.photogram.adapters.PostsAdapter;
import com.asaadna.photogram.R;
import com.asaadna.photogram.models.Post;
import com.asaadna.photogram.other.utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private PostsAdapter adapter;
    private List<Post> posts = new ArrayList<Post>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setting up the recycler view
        RecyclerView rv = view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new PostsAdapter(getContext(),posts);
        rv.setAdapter(adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get all the posts data
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        Query postsReference = firestoreDB.collection("posts").limit(20)
                .orderBy("creation_time_ms" , Query.Direction.DESCENDING);

        //Any Changes made to the collections is instnatly updated
        postsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshots == null || error != null) {
                    Log.e("Home","Error occured while fetching posts.." , error);
                } else {
                    List<Post> postList = new ArrayList<Post>();
                    //Mapping it to our model
                    for(DocumentSnapshot snapshot : documentSnapshots.getDocuments()) {
                        postList.add(snapshot.toObject(Post.class));
                    }
                    //Mutable list
                    posts.clear();
                    posts.addAll(postList);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }
}