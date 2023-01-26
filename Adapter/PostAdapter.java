package com.mustafa.message_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustafa.message_app.Models.CheckPostLike;
import com.mustafa.message_app.Models.CheckSavedImages;
import com.mustafa.message_app.Models.Post;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.RecyclerPostRowBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    ArrayList<Post> postArrayList;
    Context context;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    int flag = 0,flag2 = 0,i=0;
    private String otherName = "";

    public PostAdapter(ArrayList<Post> postArrayList, Context context) {
        this.postArrayList = postArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerPostRowBinding recyclerPostRowBinding = RecyclerPostRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(recyclerPostRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

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
                        if(postArrayList.get(position).email.equals(String.valueOf(dataSnapshot.child("email").getValue()))){
                            holder.binding.userNameTextView.setText(String.valueOf(dataSnapshot.child("userName").getValue()));
                            otherName = String.valueOf(dataSnapshot.child("userName").getValue());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

                        if(email.equals(postArrayList.get(position).email) && !downloadUrl.equals("")){
                            Glide.with(context)
                                    .load(downloadUrl)
                                    .override(40,40)
                                    .error(R.drawable.profile)
                                    .into(holder.binding.userProfileImageView);
                            flag = 1;
                        }
                    }
                }

                if(flag == 0){
                    holder.binding.userProfileImageView.setImageResource(R.drawable.profile);
                }
            }
        });

        holder.binding.postCommentText.setText(postArrayList.get(position).comment);
        holder.binding.likesTextView.setText(postArrayList.get(position).getLikes()+"\tlikes");

        Glide.with(context)
                .load(postArrayList.get(position).downloadUrl)
                .override(Resources.getSystem().getDisplayMetrics().widthPixels)
                .into(holder.binding.postImageView);

        CheckPostLike checkPostLike = new CheckPostLike();
        checkPostLike.setLikes(checkPostLike.getLikes(postArrayList.get(position).downloadUrl,postArrayList.get(position).email,firebaseUser.getEmail(),holder.binding.likesTextView,holder.binding.likeImageView));

        holder.binding.likeImageView.setOnClickListener(item -> {

            databaseReference = firebaseDatabase.getReference("Likes");

            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        if(firebaseUser.getEmail().equals(String.valueOf(dataSnapshot.child("likedUserEmail").getValue())) &&
                                postArrayList.get(position).downloadUrl.equals(String.valueOf(dataSnapshot.child("downloadUrl").getValue())) &&
                                postArrayList.get(position).email.equals(String.valueOf(dataSnapshot.child("sharedEmail").getValue()))){
                            flag2 = 1;
                        }
                    }

                    if(flag2 == 0){
                        databaseReference = firebaseDatabase.getReference("Likes");
                        HashMap<String, Object> likes = new HashMap<>();
                        likes.put("sharedEmail",postArrayList.get(position).email);
                        likes.put("likes",checkPostLike.getLikes() + 1);
                        likes.put("downloadUrl",postArrayList.get(position).downloadUrl);
                        likes.put("likedUserEmail",firebaseUser.getEmail());

                        UUID uuid = UUID.randomUUID();
                        databaseReference.child(otherName).child(String.valueOf(uuid)).setValue(likes).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                holder.binding.likeImageView.setImageResource(R.drawable.red_heart);
                                holder.binding.likesTextView.setText( checkPostLike.getLikes() +" likes");
                                notifyItemChanged(position);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.binding.likeImageView.setImageResource(R.drawable.favorite);
                            }
                        });
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

        });


        CheckSavedImages checkSavedImages = new CheckSavedImages();
        checkSavedImages.getSaved(postArrayList.get(position).downloadUrl,firebaseUser.getEmail(),postArrayList.get(position).email,holder.binding.savePostImageView);

        holder.binding.savePostImageView.setOnClickListener(item -> {


            databaseReference = firebaseDatabase.getReference("Saved");

            databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(firebaseUser.getEmail().equals(String.valueOf(dataSnapshot.child("savedEmail").getValue())) &&
                                postArrayList.get(position).downloadUrl.equals(String.valueOf(dataSnapshot.child("downloadUrl").getValue()))){
                            i = 1;
                        }
                    }

                    if(i == 0){
                        HashMap<String,Object> savedData = new HashMap<>();
                        savedData.put("sharedEmail",postArrayList.get(position).email);
                        savedData.put("downloadUrl",postArrayList.get(position).downloadUrl);
                        savedData.put("savedEmail",firebaseUser.getEmail());

                        UUID uuid = UUID.randomUUID();

                        databaseReference.child(firebaseUser.getUid()).child(String.valueOf(uuid)).setValue(savedData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                holder.binding.savePostImageView.setImageResource(R.drawable.bookmark);
                                Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, R.string.fail, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerPostRowBinding binding;
        public ViewHolder(RecyclerPostRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
