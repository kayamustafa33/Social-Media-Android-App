package com.mustafa.message_app.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafa.message_app.Adapter.SocialMediaUserImagesAdapter;
import com.mustafa.message_app.Models.SocialMediaUserImage;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.FragmentMyPicturesBinding;

import java.util.ArrayList;
import java.util.Map;

public class MyPicturesFragment extends Fragment {

    private FragmentMyPicturesBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    String forImagesEmail,userImageUrl;

    private SocialMediaUserImagesAdapter adapter;
    private ArrayList<SocialMediaUserImage> arrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyPicturesBinding.inflate(inflater,container,false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("User");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        arrayList = new ArrayList<>();
        binding.SocialMediaUserImagesRV.setLayoutManager(new GridLayoutManager(binding.getRoot().getContext(),2));
        adapter = new SocialMediaUserImagesAdapter(arrayList,binding.getRoot().getContext());
        binding.SocialMediaUserImagesRV.setAdapter(adapter);


        getData();

        return binding.getRoot();
    }

    private void getData(){
        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(binding.getRoot().getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if(value != null){
                    for(DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String,Object> data = snapshot.getData();
                        assert data != null;
                        forImagesEmail = String.valueOf(data.get("email"));
                        userImageUrl = String.valueOf(data.get("downloadUrl"));

                        if(forImagesEmail.equals(firebaseUser.getEmail()) && !userImageUrl.equals("")){
                            SocialMediaUserImage socialMediaUserImage = new SocialMediaUserImage(userImageUrl);
                            arrayList.add(socialMediaUserImage);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if(arrayList.isEmpty()){
                        binding.thereIsNoPicture.setVisibility(View.VISIBLE );
                    }
                }
            }
        });
    }
}