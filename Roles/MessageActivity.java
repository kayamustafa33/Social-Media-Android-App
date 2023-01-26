package com.mustafa.message_app.Roles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mustafa.message_app.Adapter.MessageAdapter;
import com.mustafa.message_app.Models.CheckInternet;
import com.mustafa.message_app.Models.CheckUserFollowRequest;
import com.mustafa.message_app.Models.Followers;
import com.mustafa.message_app.Models.Message;
import com.mustafa.message_app.Profiles.OtherUserMessageProfileActivity;
import com.mustafa.message_app.R;
import com.mustafa.message_app.Views.MainActivity;
import com.mustafa.message_app.databinding.ActivityMessageBinding;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    ArrayList<Message> messageArrayList;
    MessageAdapter messageAdapter;
    private String userName,otherName,otherEmail,userEmail,friendState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CheckInternet checkInternet = new CheckInternet();
        checkInternet.isOnline(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        messageArrayList = new ArrayList<>();


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Messages");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            otherName = extras.getString("otherName");
            userName = extras.getString("userName");
            otherEmail = extras.getString("otherEmail");
            userEmail = extras.getString("userEmail");
            friendState = extras.getString("followingState");

            if(otherName != null){
                getSupportActionBar().setTitle(otherName);
            }
        }

        binding.recyclerChatActivity.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageArrayList,userName,otherName);
        binding.recyclerChatActivity.setAdapter(messageAdapter);

        if(friendState.equals("Following") || friendState.equals("Takip Ediliyor")){
            getData();
        }else{
            notFriendLayout();
        }
    }

    public void sendMessage(String text){

        String time = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = LocalDateTime.now().getHour() +":"+ LocalDateTime.now().getMinute();
        }

        String key = databaseReference.child(userName).child(otherName).push().getKey();

        HashMap<String,Object> messageMap = new HashMap();
        messageMap.put("text",text);
        messageMap.put("from",otherName);
        messageMap.put("date",time);
        messageMap.put("otherEmail",otherEmail);
        messageMap.put("userEmail",firebaseUser.getEmail());

        if(!text.trim().equals("")){
            assert key != null;
            databaseReference.child(userName).child(otherName).child(key).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        databaseReference.child(otherName).child(userName).child(key).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                }
            });
        }
    }

    public void getData(){
        databaseReference.child(userName).child(otherName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(otherEmail.equals(String.valueOf(snapshot.child("otherEmail").getValue())) ||
                        otherEmail.equals(String.valueOf(snapshot.child("userEmail").getValue()))){
                    String text = String.valueOf(snapshot.child("text").getValue());
                    String from = String.valueOf(snapshot.child("from").getValue());
                    String time = String.valueOf(snapshot.child("date").getValue());
                    Message message = new Message(text,from,time,otherEmail);
                    messageArrayList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    binding.recyclerChatActivity.scrollToPosition(messageArrayList.size()-1);
                }

                messageAdapter.notifyDataSetChanged();


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

    public void sendMessageBtn(View view){
        String text = binding.messageEditText.getText().toString();
        binding.messageEditText.setText("");
        sendMessage(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_menu:
                Intent intent = new Intent(MessageActivity.this, OtherUserMessageProfileActivity.class);
                intent.putExtra("otherName",otherName);
                intent.putExtra("otherEmail",otherEmail);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void notFriendLayout(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.not_friend_layout);
        dialog.setCancelable(false);

        Button seeProfileBtn = dialog.findViewById(R.id.noFriendSeeProfileBtn);
        Button followBtn = dialog.findViewById(R.id.noFriendFollowBtn);
        CircleImageView closeImage = dialog.findViewById(R.id.noFriendCloseBtn);

        closeImage.setOnClickListener(item -> {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        seeProfileBtn.setOnClickListener(item -> {
            Toast.makeText(this, "Henüz yapım aşamasında!", Toast.LENGTH_SHORT).show();
        });


        if(friendState.equals("Request pending") || friendState.equals("İstek beklemede")){
            followBtn.setText(R.string.request_pending);
        }

        CheckUserFollowRequest checkUserFollowRequest = new CheckUserFollowRequest();
        checkUserFollowRequest.checkRequest(followBtn,otherName,otherEmail,userName);

        followBtn.setOnClickListener(item -> {
            Followers followers = new Followers();

            if(followBtn.getText().toString().equals("Follow") || followBtn.getText().toString().equals("Takip Et")){
                followers.setFollowers(getApplicationContext(),followBtn,otherEmail,otherName, firebaseUser.getEmail(),userName);
            }

            if(followBtn.getText().toString().equals("Sent Request") || followBtn.getText().toString().equals("İstek Gönderildi")){
                final Dialog dialog2 = new Dialog(this);
                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog2.setContentView(R.layout.cancel_friend_request);

                LinearLayout cancelRequest = dialog2.findViewById(R.id.cancelFriendRequestLayout);

                cancelRequest.setOnClickListener(item2 -> {
                    dialog2.dismiss();
                    databaseReference = firebaseDatabase.getReference("Follow Request");
                    com.google.firebase.database.Query query = databaseReference.child(otherName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    String requestName = String.valueOf(dataSnapshot.child("otherName").getValue());
                                    String requestEmail = String.valueOf(dataSnapshot.child("otherEmail").getValue());
                                    String senderName = String.valueOf(dataSnapshot.child("senderName").getValue());
                                    String senderEmail = String.valueOf(dataSnapshot.child("senderEmail").getValue());

                                    if(requestName.equals(otherName) &&
                                            requestEmail.equals(otherEmail) &&
                                            senderName.equals(userName) &&
                                            senderEmail.equals(userEmail)){
                                        Toast.makeText(MessageActivity.this, R.string.friend_request_canceled, Toast.LENGTH_SHORT).show();
                                        dataSnapshot.getRef().removeValue();
                                        followBtn.setClickable(false);
                                    }
                                }
                                messageAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                });

                dialog2.show();
                dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog2.getWindow().setGravity(Gravity.BOTTOM);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }
}