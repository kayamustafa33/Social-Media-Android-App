package com.mustafa.message_app.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.mustafa.message_app.Adapter.TabLayoutAdapter;
import com.mustafa.message_app.Models.CheckInternet;
import com.mustafa.message_app.R;
import com.mustafa.message_app.Profiles.MessageAppProfileActivity;
import com.mustafa.message_app.Roles.SocialMediaActivity;
import com.mustafa.message_app.databinding.ActivityMainBinding;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TabLayoutAdapter tabLayoutAdapter;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private MaterialToolbar toolbar;
    private ImageView backImage,profileImage;
    private DatabaseReference databaseReference;
    private TextView headerUserNameText;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        CheckInternet checkInternet = new CheckInternet();
        checkInternet.isOnline(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("User");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore  = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        tabLayoutAdapter = new TabLayoutAdapter(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(item -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationViewItems();
        updateNavHeader();

        binding.viewPager2.setAdapter(tabLayoutAdapter);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.getTabAt(position).select();
            }
        });

    }

    private void navigationViewItems(){

        binding.navigationView.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){

                case R.id.messageAppProfile:
                    binding.drawerLayout.close();
                    startActivity(new Intent(MainActivity.this, MessageAppProfileActivity.class));
                    break;
                case R.id.logout:
                    binding.drawerLayout.close();
                    auth.signOut();
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                    break;

                case R.id.socialMedia:
                    binding.drawerLayout.close();
                    startActivity(new Intent(MainActivity.this, SocialMediaActivity.class));
                    break;
            }
            return false;
        });
    }

    private void updateNavHeader(){
        NavigationView navigationView = findViewById(R.id.navigationView);
        View viewHeader = navigationView.getHeaderView(0);

        backImage = viewHeader.findViewById(R.id.backBtn);
        headerUserNameText = viewHeader.findViewById(R.id.userNameHeaderText);
        profileImage = viewHeader.findViewById(R.id.header_profile_image);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(Objects.requireNonNull(firebaseUser.getEmail()).equals(String.valueOf(dataSnapshot.child("email").getValue()))){
                            headerUserNameText.setText(String.valueOf(dataSnapshot.child("userName").getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                headerUserNameText.setText(R.string.error);
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
                        String email = String.valueOf(data.get("email"));
                        String downloadUrl = String.valueOf(data.get("downloadUrl"));

                        if(email.equals(firebaseUser.getEmail()) && !downloadUrl.equals("")){
                            Glide.with(getApplicationContext())
                                    .load(downloadUrl)
                                    .override(120, 120)
                                    .error(R.drawable.profile)
                                    .into(profileImage);
                            flag = 1;
                        }
                    }
                    if(flag == 0){
                        profileImage.setImageResource(R.drawable.profile);
                    }
                }

                if(value == null){
                    profileImage.setImageResource(R.drawable.profile);
                }
            }
        });

        backImage.setOnClickListener(item -> {
            binding.drawerLayout.close();
        });
    }

}