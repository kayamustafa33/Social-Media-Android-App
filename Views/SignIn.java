package com.mustafa.message_app.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignIn extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private String email,password;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if(firebaseUser != null){
            IntentUser();
        }
    }

    public void signInBtn(View view){
        email = binding.userEmailSignUp.getText().toString().trim();
        password = binding.userPasswordSignUp.getText().toString().trim();

        if(!email.equals("") && !password.equals("")){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(SignIn.this, R.string.successful, Toast.LENGTH_SHORT).show();
                    IntentUser();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignIn.this, "Invalid User", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(this, R.string.fill_requirements, Toast.LENGTH_LONG).show();
        }
    }

    public void singInTextClicked(View view){
        startActivity(new Intent(SignIn.this, SignUp.class));
    }

    public void forgotPassword(View view){
        //Forgot Password
    }

    private void IntentUser(){
        Intent intent = new Intent(SignIn.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }
}