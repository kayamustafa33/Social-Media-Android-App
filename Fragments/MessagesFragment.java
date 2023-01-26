package com.mustafa.message_app.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mustafa.message_app.Adapter.UserLayoutAdapter;
import com.mustafa.message_app.Models.MessageAppViewModel;
import com.mustafa.message_app.Models.User;
import com.mustafa.message_app.databinding.FragmentMessagesBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MessagesFragment extends Fragment {

    private FragmentMessagesBinding binding;
    private ArrayList<User> arrayList;
    private UserLayoutAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MessageAppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater,container,false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("User");

        arrayList = new ArrayList<>();
        binding.messagesRV.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        adapter = new UserLayoutAdapter(arrayList,binding.getRoot().getContext());
        binding.messagesRV.setAdapter(adapter);

        getData();
        setViewModel();
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MessageAppViewModel.class);
    }

    private void getData(){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(!Objects.equals(firebaseUser.getEmail(), String.valueOf(dataSnapshot.child("email").getValue()))){
                            String email = String.valueOf(dataSnapshot.child("email").getValue());
                            String name = String.valueOf(dataSnapshot.child("userName").getValue());
                            String password = String.valueOf(dataSnapshot.child("password"));
                            User user = new User(name,email,password);
                            arrayList.add(user);
                        }
                    }

                    if(arrayList.isEmpty()){
                        binding.thereIsNoFriend.setVisibility(View.VISIBLE);
                    }else{
                        binding.thereIsNoFriend.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(binding.getRoot().getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setViewModel(){
        databaseReference = firebaseDatabase.getReference("User");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(firebaseUser.getEmail().equals(String.valueOf(dataSnapshot.child("email").getValue()))){
                            viewModel.setUsername(String.valueOf(dataSnapshot.child("userName").getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}