package com.mustafa.message_app.Adapter;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafa.message_app.Models.Followers;
import com.mustafa.message_app.Models.User;
import com.mustafa.message_app.R;
import com.mustafa.message_app.Roles.MessageActivity;
import com.mustafa.message_app.databinding.RecyclerUserRowBinding;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UserLayoutAdapter extends RecyclerView.Adapter<UserLayoutAdapter.ViewHolder> {

    ArrayList<User> userArrayList;
    Context context;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    String senderName="",senderEmail="";
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    int flag = 0;

    public UserLayoutAdapter(ArrayList<User> userArrayList, Context context) {
        this.userArrayList = userArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserLayoutAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerUserRowBinding recyclerUserRowBinding = RecyclerUserRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(recyclerUserRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserLayoutAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.personName.setText(userArrayList.get(position).userName);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("User");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(Objects.equals(firebaseUser.getEmail(), String.valueOf(dataSnapshot.child("email").getValue()))){
                            senderName = String.valueOf(dataSnapshot.child("userName").getValue());
                            senderEmail = String.valueOf(dataSnapshot.child("email").getValue());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Followers checkFollower = new Followers();
        checkFollower.getFollowers(context,holder.binding.followBtn,userArrayList.get(position).userName,userArrayList.get(position).email,senderName,senderEmail);

        checkFriend(userArrayList.get(position).userName,holder.binding.followBtn);
        checkFriendRequest(senderName,userArrayList.get(position).email,holder.binding.followBtn);


        firebaseFirestore.collection("UserProfile").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null){
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()){
                        Map<String,Object> data = documentSnapshot.getData();
                        assert data != null;
                        String email = String.valueOf(data.get("email"));
                        String downloadUrl = String.valueOf(data.get("downloadUrl"));

                        if(email.equals(userArrayList.get(position).email)){
                            Glide.with(context)
                                    .load(downloadUrl)
                                    .override(50,50)
                                    .error(R.drawable.profile)
                                    .into(holder.binding.profileImage);
                        }else{
                            holder.binding.profileImage.setImageResource(R.drawable.profile);

                        }
                    }

                }
            }
        });



        holder.binding.followBtn.setOnClickListener(item -> {
            Followers followers = new Followers();
            if(holder.binding.followBtn.getText().toString().equals("Follow") || holder.binding.followBtn.getText().toString().equals("Takip Et")){
                followers.setFollowers(context,holder.binding.followBtn,userArrayList.get(position).email,userArrayList.get(position).userName,firebaseUser.getEmail(),senderName);
            }

            if(holder.binding.followBtn.getText().toString().equals("Following") || holder.binding.followBtn.getText().toString().equals("Takip Ediliyor")){
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.unfollow_bottom_sheet);

                LinearLayout unfollow = dialog.findViewById(R.id.layout_unfollow);
                unfollow.setOnClickListener(item2 -> {
                    //unfollow user
                    Toast.makeText(context, "Yapım aşamasında!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);

            }

            if(holder.binding.followBtn.getText().toString().equals("Sent Request") || holder.binding.followBtn.getText().toString().equals("İstek Gönderildi")){
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.cancel_friend_request);

                LinearLayout cancelRequestLayout = dialog.findViewById(R.id.cancelFriendRequestLayout);

                cancelRequestLayout.setOnClickListener(item2 -> {
                    dialog.dismiss();


                    databaseReference = firebaseDatabase.getReference("Follow Request");
                    com.google.firebase.database.Query query = databaseReference.child(userArrayList.get(position).userName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                                    String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                                    String senderName2 = String.valueOf(dataSnapshot.child("senderName").getValue());
                                    String senderEmail2 = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                                    if(requestName.equals(userArrayList.get(position).userName) &&
                                            requestEmail.equals(userArrayList.get(position).email) &&
                                            senderName2.equals(senderName) &&
                                            senderEmail2.equals(senderEmail)){

                                        dataSnapshot.getRef().removeValue();
                                        holder.binding.followBtn.setText(R.string.follow);
                                        holder.binding.followBtn.setClickable(false);
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

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }

        });


        holder.itemView.setOnClickListener(item -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("otherName",userArrayList.get(position).userName);
            intent.putExtra("userName",senderName);
            intent.putExtra("otherEmail",userArrayList.get(position).email);
            intent.putExtra("userEmail",firebaseUser.getEmail());
            intent.putExtra("followingState",holder.binding.followBtn.getText().toString());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerUserRowBinding binding;
        public ViewHolder(RecyclerUserRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void checkFriend(String username, Button button){
        databaseReference = firebaseDatabase.getReference("Friends");
        databaseReference.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String friendName = String.valueOf(dataSnapshot.child("friendName").getValue());
                        String friendEmail = String.valueOf(dataSnapshot.child("friendEmail").getValue());

                        if(friendEmail.equals(senderEmail) && friendName.equals(senderName)){
                            button.setText(R.string.following);
                            int color = ContextCompat.getColor(context,R.color.green);
                            button.setTextColor(color);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkFriendRequest(String username,String otherEmail,Button button){
        databaseReference = firebaseDatabase.getReference("Follow Request");
        firebaseUser = auth.getCurrentUser();
        databaseReference.child(username).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                    String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                    if(senderEmail.equals(otherEmail) && requestEmail.equals(firebaseUser.getEmail())){
                        button.setText(R.string.request_pending);
                        int color = ContextCompat.getColor(context,R.color.orange);
                        button.setTextColor(color);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
