package com.mustafa.message_app.Models;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mustafa.message_app.R;

public class CheckPostLike {

    private int likes = 0;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;

    public int getLikes(String downloadUrl, String postEmail, String userEmail, TextView likeText, ImageView likeImage){

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Likes");

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        if(userEmail.equals(String.valueOf(dataSnapshot.child("likedUserEmail").getValue())) &&
                                downloadUrl.equals(String.valueOf(dataSnapshot.child("downloadUrl").getValue())) &&
                                postEmail.equals(String.valueOf(dataSnapshot.child("sharedEmail").getValue()))){
                            likeImage.setImageResource(R.drawable.red_heart);
                        }

                        if(downloadUrl.equals(String.valueOf(dataSnapshot.child("downloadUrl").getValue()))){
                            likes++;
                        }

                    }
                    likeText.setText(likes + " likes");
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
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLikes() {
        return likes;
    }

}
