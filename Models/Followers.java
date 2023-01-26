package com.mustafa.message_app.Models;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
import com.mustafa.message_app.R;

import java.util.HashMap;
import java.util.UUID;

public class Followers {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    public String  newRequest = "1";
    public String oldRequest = "0";
    public int flag = 0;

    public void setFollowers(Context context, Button textBtn, String otherEmail, String otherName,String senderEmail,String senderName){
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Follow Request");

        UUID uuid = UUID.randomUUID();
        databaseReference.child(otherName).child(String.valueOf(uuid)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                    String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());

                    if(requestName.equals(otherName) && requestEmail.equals(otherEmail)){
                        flag = 1;
                    }
                }

                if(flag == 0){
                    HashMap<String,Object> setFollowersData = new HashMap<>();
                    setFollowersData.put("otherEmail",otherEmail);
                    setFollowersData.put("request",newRequest);
                    setFollowersData.put("otherName",otherName);
                    setFollowersData.put("senderEmail",senderEmail);
                    setFollowersData.put("senderName",senderName);

                    databaseReference.child(otherName).child(String.valueOf(uuid)).setValue(setFollowersData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            textBtn.setText(R.string.send_request);
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

    }

    public void getFollowers(Context context,Button textBtn,String otherName,String otherEmail,String senderName,String senderEmail){
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Follow Request");

        databaseReference.child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                        String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                        String senderName2 = String.valueOf(dataSnapshot.child("senderName").getValue());
                        String senderEmail2 = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                        if(senderEmail2.equals(firebaseUser.getEmail()) && requestEmail.equals(otherEmail)){
                            textBtn.setText(R.string.send_request);
                            int color = ContextCompat.getColor(context,R.color.red);
                            textBtn.setTextColor(color);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void unFollow(){

    }
}
