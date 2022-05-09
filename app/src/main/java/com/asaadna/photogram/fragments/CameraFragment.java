package com.asaadna.photogram.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.asaadna.photogram.R;
import com.asaadna.photogram.models.Post;
import com.asaadna.photogram.models.User;
import com.asaadna.photogram.other.utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class CameraFragment extends Fragment {

    public final static int PICK_PHOTO_CODE = 1046;

    private ImageView imageView;
    private Uri photoURI = null;

    private FirebaseFirestore firestoreDB;
    private User signedInUser = null;


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PHOTO_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    photoURI = data.getData();
                    imageView.setImageURI(photoURI);
                    utils.toastDebug(getContext(),"Photo loaded !");
                }
            } else {
                utils.toastDebug(getContext(),"Picking image cancelled");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.imagePreview);
        EditText description = view.findViewById(R.id.descriptionEditText);

        //Upload Button
        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Disbale Upload Button
                uploadButton.setEnabled(false);

               if(photoURI == null) {
                   utils.toastDebug(getContext(),"Select an image first !!");
               } else if(description.getText().toString().isEmpty()){
                   utils.toastDebug(getContext(),"Add a description !!");
               } else if(signedInUser == null) {
                   utils.toastDebug(getContext(),"No User Signed In :( !!");
               } else {

                   //Retrieve image url of the uploaded image
                   //Create a post object with the image url and add that to the posts collection in firestore

                   StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                   StorageReference photoRef = storageReference.child("images/"+System.currentTimeMillis()+"-photo.jpg");

                   photoRef.putFile(photoURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   photoURI = uri;
                                   utils.toastDebug(getContext(),"Uploaded " + photoURI);

                                   Post p = new Post();
                                   p.user = signedInUser;
                                   p.description = description.getText().toString();
                                   p.image_url = uri.toString();
                                   p.creation_time_ms = System.currentTimeMillis();

                                   firestoreDB.collection("posts").add(p).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                       @Override
                                       public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(!task.isSuccessful()) {
                                                Log.e("Camera","Error at uploading",task.getException());
                                                utils.toastDebug(getContext(),"Could not Upload Post");
                                            } else {
                                                utils.toastDebug(getContext(),"New Post Uploaded !!");
                                                uploadButton.setEnabled(true);
                                                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
                                            }
                                       }
                                   });
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   utils.toastDebug(getContext(),"Error Uploading Photo");
                                   Log.e("Camera",e.toString());
                                   uploadButton.setEnabled(true);
                               }
                           });
                       }
                   });
               }
            }
        });

        //Choose Image Button
        Button chooseImageButton = view.findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if(intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(intent,PICK_PHOTO_CODE);
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the signedin user here
        firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                signedInUser = documentSnapshot.toObject(User.class);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera,container,false);
    }
}
