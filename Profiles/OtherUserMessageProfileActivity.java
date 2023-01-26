package com.mustafa.message_app.Profiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.mustafa.message_app.Adapter.TabLayoutAdapter;
import com.mustafa.message_app.Models.CheckInternet;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.ActivityOtherUserMessageProfileBinding;

import java.util.Objects;

public class OtherUserMessageProfileActivity extends AppCompatActivity {

    private ActivityOtherUserMessageProfileBinding binding;
    private String otherName,otherEmail;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherUserMessageProfileBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        CheckInternet checkInternet = new CheckInternet();
        checkInternet.isOnline(this);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            otherName = extras.getString("otherName");
            otherEmail = extras.getString("otherEmail");
            binding.otherEmailText.setText(otherEmail);
            binding.otherUserNameText.setText(otherName);
            getSupportActionBar().setTitle(otherName);
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(item -> onBackPressed());
        if(otherName != null){
            toolbar.setTitle(otherName);
        }
    }

    public void friendsClicked(View view){
        Toast.makeText(this, "Yapım aşamasında!", Toast.LENGTH_SHORT).show();
    }
}