package com.mustafa.message_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafa.message_app.Models.FollowRequest;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.RecyclerFriendRequestRowBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {

    private ArrayList<FollowRequest> requestArrayList;
    private Context context;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private int flag = 0;
    private int flag2 = 0;

    public FriendRequestsAdapter(ArrayList<FollowRequest> requestArrayList,Context context){
        this.requestArrayList = requestArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendRequestsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerFriendRequestRowBinding recyclerFriendRequestRowBinding = RecyclerFriendRequestRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(recyclerFriendRequestRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.requestName.setText(requestArrayList.get(position).senderName);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseFirestore.collection("UserProfile").orderBy("date", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if(value != null){
                    for(DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String,Object> data = snapshot.getData();
                        assert data != null;
                        String email = String.valueOf(data.get("email"));
                        String downloadUrl = String.valueOf(data.get("downloadUrl"));

                        if(email.equals(requestArrayList.get(position).senderEmail) && !downloadUrl.equals("")){
                            Glide.with(context)
                                    .load(downloadUrl)
                                    .override(40,40)
                                    .error(R.drawable.profile)
                                    .into(holder.binding.requestProfile);
                            flag = 1;
                        }
                    }
                }
                if(flag == 0){
                    holder.binding.requestProfile.setImageResource(R.drawable.profile);
                }
            }
        });

        holder.binding.acceptRequestBtn.setOnClickListener(item -> {

            UUID uuid = UUID.randomUUID();

            HashMap<String,Object> friendData = new HashMap<>();
            friendData.put("friendName",requestArrayList.get(position).senderName);
            friendData.put("friendEmail",requestArrayList.get(position).senderEmail);
            friendData.put("adminName",requestArrayList.get(position).requestName);
            friendData.put("adminEmail",requestArrayList.get(position).requestEmail);

            databaseReference = firebaseDatabase.getReference("Friends");
            databaseReference.child(requestArrayList.get(position).requestName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String friendName = String.valueOf(dataSnapshot.child("friendName").getValue());
                            String friendEmail = String.valueOf(dataSnapshot.child("friendEmail").getValue());
                            if(friendEmail.equals(requestArrayList.get(position).senderEmail) && friendName.equals(requestArrayList.get(position).senderName)){
                                flag2 = 1;
                            }
                        }
                        if(flag2 == 0){
                            databaseReference.child(requestArrayList.get(position).requestName).child(String.valueOf(uuid)).setValue(friendData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    HashMap<String,Object> friendData2 = new HashMap<>();
                                    friendData2.put("friendName",requestArrayList.get(position).requestName);
                                    friendData2.put("friendEmail",requestArrayList.get(position).requestEmail);
                                    friendData2.put("adminName",requestArrayList.get(position).senderName);
                                    friendData2.put("adminEmail",requestArrayList.get(position).senderEmail);
                                    databaseReference.child(requestArrayList.get(position).senderName).child(String.valueOf(uuid)).setValue(friendData2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            databaseReference = firebaseDatabase.getReference("Follow Request");
                                            com.google.firebase.database.Query query = databaseReference.child(requestArrayList.get(position).requestName);
                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                            String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                                                            String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                                                            String senderName = String.valueOf(dataSnapshot.child("senderName").getValue());
                                                            String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                                                            if(requestName.equals(requestArrayList.get(position).requestName) &&
                                                                    requestEmail.equals(requestArrayList.get(position).requestEmail) &&
                                                                    senderName.equals(requestArrayList.get(position).senderName) &&
                                                                    senderEmail.equals(requestArrayList.get(position).senderEmail)){

                                                                dataSnapshot.getRef().removeValue();
                                                                holder.binding.rejectRequestBtn.setVisibility(View.INVISIBLE);
                                                                holder.binding.acceptRequestBtn.setVisibility(View.INVISIBLE);
                                                            }
                                                        }
                                                        notifyDataSetChanged();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });
        
        holder.binding.rejectRequestBtn.setOnClickListener(item -> {
            databaseReference = firebaseDatabase.getReference("Follow Request");
            com.google.firebase.database.Query query = databaseReference.child(requestArrayList.get(position).requestName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                            String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                            String senderName = String.valueOf(dataSnapshot.child("senderName").getValue());
                            String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                            if(requestName.equals(requestArrayList.get(position).requestName) &&
                                    requestEmail.equals(requestArrayList.get(position).requestEmail) &&
                                    senderName.equals(requestArrayList.get(position).senderName) &&
                                    senderEmail.equals(requestArrayList.get(position).senderEmail)){

                                dataSnapshot.getRef().removeValue();
                                holder.binding.acceptRequestBtn.setVisibility(View.INVISIBLE);
                                holder.binding.rejectRequestBtn.setVisibility(View.INVISIBLE);
                            }
                        }
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return requestArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerFriendRequestRowBinding binding;
        public ViewHolder(RecyclerFriendRequestRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
