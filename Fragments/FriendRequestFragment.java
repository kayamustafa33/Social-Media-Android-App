package com.mustafa.message_app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mustafa.message_app.Adapter.FriendRequestsAdapter;
import com.mustafa.message_app.Models.FollowRequest;
import com.mustafa.message_app.Models.Followers;
import com.mustafa.message_app.Models.MessageAppViewModel;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.FragmentFriendRequestBinding;

import java.util.ArrayList;
import java.util.Objects;

public class FriendRequestFragment extends Fragment {

    private FragmentFriendRequestBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<FollowRequest> followRequestArrayList;
    private FriendRequestsAdapter adapter;
    MessageAppViewModel viewModel;
    String adminName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFriendRequestBinding.inflate(inflater,container,false);
        followRequestArrayList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        binding.requestRV.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        adapter = new FriendRequestsAdapter(followRequestArrayList,binding.getRoot().getContext());
        binding.requestRV.setAdapter(adapter);

        adminName = viewModel.getUsername().getValue();
        if(adminName != null){
            getData();
        }


        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MessageAppViewModel.class);
    }

    public void getData(){
        databaseReference = firebaseDatabase.getReference("Follow Request");
        databaseReference.child(adminName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                        String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                        String  requestNumber = String.valueOf(dataSnapshot.child("request").getValue());
                        String senderName = String.valueOf(dataSnapshot.child("senderName").getValue());
                        String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                        if(requestEmail.equals(firebaseUser.getEmail())){
                            FollowRequest followRequest = new FollowRequest(requestEmail,requestName,senderEmail,senderName,requestNumber);
                            followRequestArrayList.add(followRequest);
                        }

                    }

                    if(!followRequestArrayList.isEmpty()){
                        binding.noRequestText.setVisibility(View.INVISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}