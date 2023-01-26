package com.mustafa.message_app.Profiles;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mustafa.message_app.Models.CheckInternet;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.ActivityMessageAppProfileBinding;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class    MessageAppProfileActivity extends AppCompatActivity {

    private ActivityMessageAppProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri imageData;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private String username = "";
    private int friendCount = 0;
    String email,downloadUrl;
    private double progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageAppProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CheckInternet checkInternet = new CheckInternet();
        checkInternet.isOnline(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("User");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();


        binding.cancelAction.setOnClickListener(item -> {
            binding.actionProfileLayout.setVisibility(View.GONE);
            //check the user current profile image.
            getUserData();
        });

        binding.saveAction.setOnClickListener(item -> {
            updateProfileImage();
            binding.actionProfileLayout.setVisibility(View.GONE);
        });

        getUserData();
        registerLauncher();

    }

    public void getUserData(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(Objects.equals(firebaseUser.getEmail(), String.valueOf(dataSnapshot.child("email").getValue()))){
                            binding.userNameProfileText.setText(String.valueOf(dataSnapshot.child("userName").getValue()));
                            binding.emailProfileText.setText(String.valueOf(dataSnapshot.child("email").getValue()));
                            username = String.valueOf(dataSnapshot.child("userName").getValue());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageAppProfileActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
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
                         email = String.valueOf(data.get("email"));
                         downloadUrl = String.valueOf(data.get("downloadUrl"));

                        if(email.equals(firebaseUser.getEmail()) && !downloadUrl.equals("")){
                            Glide.with(getApplicationContext())
                                    .load(downloadUrl)
                                    .override(120, 120)
                                    .error(R.drawable.profile)
                                    .into(binding.profileImage);
                        }
                    }
                }

                if(value == null){
                    binding.profileImage.setImageResource(R.drawable.profile);
                }
            }
        });

        databaseReference = firebaseDatabase.getReference("Friends");
        databaseReference.child(username).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String userEmail = String.valueOf(dataSnapshot.child("adminEmail").getValue());
                        if(userEmail.equals(firebaseUser.getEmail())){
                            friendCount++;
                        }
                    }
                }

                binding.friendsCountText.setText(String.valueOf(friendCount));
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

    public void setTheProfileImage(View view){

        final Dialog dialog = new Dialog(binding.getRoot().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout selectNewProfile = dialog.findViewById(R.id.layout_new_picture);
        LinearLayout showPicture = dialog.findViewById(R.id.layout_show_picture);

        selectNewProfile.setOnClickListener(item -> {
            chooseImageClicked();
            binding.actionProfileLayout.setVisibility(View.VISIBLE);
            dialog.dismiss();
        });

        showPicture.setOnClickListener(item -> {
            dialog.dismiss();
            final Dialog dialog1 = new Dialog(binding.getRoot().getContext());
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.show_user_profile_layout);

            ImageView closeLayout = dialog1.findViewById(R.id.showUserProfileCloseLayoutImage);
            ImageView showUserProfile = dialog1.findViewById(R.id.showUserProfileImageView);
            TextView failText = dialog1.findViewById(R.id.failTextView);

            if(downloadUrl != null){
                Glide.with(getApplicationContext())
                        .load(downloadUrl)
                        .error(R.drawable.profile)
                        .into(showUserProfile);
            }else{
                failText.setVisibility(View.VISIBLE);
            }

            closeLayout.setOnClickListener(item2 ->{
                dialog1.dismiss();
            });

            dialog1.show();
            dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void chooseImageClicked(){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(binding.getRoot(), R.string.permission_gallery,Snackbar.LENGTH_INDEFINITE).setAction(R.string.give_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //ask permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }else{
                    //ask permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
    }

    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        imageData = intentFromResult.getData();
                        binding.profileImage.setImageURI(imageData);

                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else {
                    Toast.makeText(getApplicationContext(), R.string.permission_needed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfileImage(){

            if(imageData != null){

                final ProgressDialog progressDialog = new ProgressDialog(binding.getRoot().getContext());
                progressDialog.setTitle(getString(R.string.image_isloading));
                progressDialog.setCancelable(true);
                progressDialog.show();

                //universal unique id
                UUID uuid = UUID.randomUUID();
                String imageName = "profileImages/" + uuid + ".jpg";
                storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Download Url
                        StorageReference newReference = firebaseStorage.getReference(imageName);
                        newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                String email = firebaseUser.getEmail();

                                HashMap<String,Object> data = new HashMap<>();
                                data.put("downloadUrl",downloadUrl);
                                data.put("email",email);
                                data.put("date", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("UserProfile").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        if(progress == 100){
                                            progressDialog.dismiss();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(binding.getRoot().getContext(), R.string.fail, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(binding.getRoot().getContext(), R.string.fail, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                        progressDialog.setMessage(getString(R.string.image_uploaded)+(int) progress+"%");
                    }
                });
            }else{
                Toast.makeText(this, R.string.select_profile_image, Toast.LENGTH_SHORT).show();
            }
    }

    public void friendsClicked(View view){
        Toast.makeText(this, "Yapım aşamasında!", Toast.LENGTH_SHORT).show();
    }
}