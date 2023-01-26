package com.mustafa.message_app.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mustafa.message_app.R;
import com.mustafa.message_app.Roles.SocialMediaActivity;
import com.mustafa.message_app.databinding.FragmentSocialMediaShareBinding;

import java.util.HashMap;
import java.util.UUID;

public class SocialMediaShareFragment extends Fragment {

    private FragmentSocialMediaShareBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri imageData;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    private double progress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSocialMediaShareBinding.inflate(inflater,container,false);

        registerLauncher();

        chooseImageClicked();
        shareButtonClicked();

        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        return binding.getRoot();
    }

    private void chooseImageClicked(){
        binding.chooseImage.setOnClickListener(item -> {

            if(ContextCompat.checkSelfPermission(item.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(item, R.string.permission_gallery,Snackbar.LENGTH_INDEFINITE).setAction(R.string.give_permission, new View.OnClickListener() {
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

        });
    }

    private void shareButtonClicked(){
        binding.shareBtnClicked.setOnClickListener(item -> {

            if(imageData != null){

                final ProgressDialog progressDialog = new ProgressDialog(binding.getRoot().getContext());
                progressDialog.setTitle(getString(R.string.image_isloading));
                progressDialog.setCancelable(true);
                progressDialog.show();

                String comment = binding.userShareComment.getText().toString();
                binding.shareBtnClicked.setVisibility(View.INVISIBLE);
                binding.userShareComment.setText("");
                binding.chooseImage.setClickable(false);
                //universal unique id
                UUID uuid = UUID.randomUUID();
                String imageName = "images/" + uuid + ".jpg";
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
                                int likes = 0;

                                HashMap<String,Object> data = new HashMap<>();
                                data.put("downloadUrl",downloadUrl);
                                data.put("comment",comment);
                                data.put("email",email);
                                data.put("likes",likes);
                                data.put("date", FieldValue.serverTimestamp());


                                firebaseFirestore.collection("Posts").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        if(progress == 100){
                                            progressDialog.dismiss();
                                            binding.chooseImage.setImageResource(R.drawable.add_image);
                                            binding.chooseImage.setClickable(true);
                                            binding.shareBtnClicked.setVisibility(View.VISIBLE);
                                            binding.userShareComment.setText("");
                                            startActivity(new Intent(requireActivity(),SocialMediaActivity.class));
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
            }

        });
    }

    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == requireActivity().RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        imageData = intentFromResult.getData();
                        binding.chooseImage.setImageURI(imageData);
                        binding.didNotSelectPictureText.setVisibility(View.GONE);

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
                    Toast.makeText(binding.getRoot().getContext(), R.string.permission_needed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}