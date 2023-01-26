package com.mustafa.message_app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafa.message_app.Adapter.SocialMediaTabLayoutAdapter;
import com.mustafa.message_app.Adapter.SocialMediaUserImagesAdapter;
import com.mustafa.message_app.Adapter.TabLayoutAdapter;
import com.mustafa.message_app.Models.SocialMediaUserImage;
import com.mustafa.message_app.Profiles.MessageAppProfileActivity;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.FragmentSocialMediaProfileBinding;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SocialMediaProfileFragment extends Fragment {

    private FragmentSocialMediaProfileBinding binding;
    private SocialMediaUserImagesAdapter adapter;
    private ArrayList<SocialMediaUserImage> arrayList;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    String username,forProfileEmail,profileImageDownloadUrl;

    private SocialMediaTabLayoutAdapter tabLayoutAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSocialMediaProfileBinding.inflate(inflater,container,false);



        tabLayoutAdapter = new SocialMediaTabLayoutAdapter(requireActivity());

        binding.SocialMediaViewPager.setAdapter(tabLayoutAdapter);
        binding.socialMediaProfileTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.SocialMediaViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.SocialMediaViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.socialMediaProfileTabLayout.getTabAt(position).select();
            }
        });


        getData();
        return binding.getRoot();
    }

    private void getData(){
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("User");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(Objects.equals(firebaseUser.getEmail(), String.valueOf(dataSnapshot.child("email").getValue()))){
                            binding.socialMediaProfileUserName.setText(String.valueOf(dataSnapshot.child("userName").getValue()));
                            binding.socialMediaProfileEmail.setText(String.valueOf(dataSnapshot.child("email").getValue()));
                            username = String.valueOf(dataSnapshot.child("userName").getValue());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(binding.getRoot().getContext(), R.string.fail, Toast.LENGTH_SHORT).show();
            }
        });

        firebaseFirestore.collection("UserProfile").orderBy("date", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(binding.getRoot().getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if(value != null){
                    for(DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String,Object> data = snapshot.getData();
                        assert data != null;
                        forProfileEmail = String.valueOf(data.get("email"));
                        profileImageDownloadUrl = String.valueOf(data.get("downloadUrl"));

                        if(forProfileEmail.equals(firebaseUser.getEmail()) && !profileImageDownloadUrl.equals("")){
                            Glide.with(binding.getRoot().getContext())
                                    .load(profileImageDownloadUrl)
                                    .override(120, 120)
                                    .error(R.drawable.profile)
                                    .into(binding.socialMediaProfileImage);
                        }
                    }
                }

                if(value == null){
                    binding.socialMediaProfileImage.setImageResource(R.drawable.profile);
                }
            }
        });

    }
}