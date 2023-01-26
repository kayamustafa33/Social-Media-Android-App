package com.mustafa.message_app.Models;

import android.widget.Button;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mustafa.message_app.R;

public class CheckUserFollowRequest {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    int flag = 0;

    public int checkRequest(Button button,String otherName,String otherEmail,String username){
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Follow Request");

        databaseReference.child(otherName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String senderName = String.valueOf(dataSnapshot.child("senderName").getValue());
                        String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());
                        String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());

                        if(senderName.equals(username) && senderEmail.equals(firebaseUser.getEmail()) && requestEmail.equals(otherEmail)){
                            flag = 1;
                            button.setText(R.string.send_request);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return flag;
    }
}
