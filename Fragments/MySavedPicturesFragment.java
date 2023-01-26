package com.mustafa.message_app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mustafa.message_app.Adapter.UserSavedImagesAdapter;
import com.mustafa.message_app.Models.UserSavedImages;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.FragmentMySavedPicturesBinding;

import java.util.ArrayList;

public class MySavedPicturesFragment extends Fragment {

    private FragmentMySavedPicturesBinding binding;
    private ArrayList<UserSavedImages> arrayList;
    private UserSavedImagesAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMySavedPicturesBinding.inflate(inflater,container,false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Saved");

        arrayList = new ArrayList<>();

        binding.savedImagesRV.setLayoutManager(new GridLayoutManager(binding.getRoot().getContext(),2));
        adapter = new UserSavedImagesAdapter(arrayList,binding.getRoot().getContext());
        binding.savedImagesRV.setAdapter(adapter);

        getData();

        return binding.getRoot();
    }

    private void getData(){
        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String downloadUrl = String.valueOf(dataSnapshot.child("downloadUrl").getValue());
                        String savedEmail = String.valueOf(dataSnapshot.child("savedEmail").getValue());
                        String sharedEmail = String.valueOf(dataSnapshot.child("sharedEmail").getValue());
                        String sharedName = String.valueOf(dataSnapshot.child("sharedName").getValue());

                        UserSavedImages userSavedImages = new UserSavedImages(downloadUrl,savedEmail,sharedEmail,sharedName);
                        arrayList.add(userSavedImages);
                    }
                    adapter.notifyDataSetChanged();

                    if(arrayList.isEmpty()){
                        binding.thereIsNoSavedPicture.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}