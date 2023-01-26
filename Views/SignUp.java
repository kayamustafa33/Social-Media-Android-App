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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mustafa.message_app.Models.User;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.ActivitySignInBinding;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth auth;
    private String username,email,password,confirmPassword;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("User");
    }

    public void signUpBtn(View view){
        reference = FirebaseDatabase.getInstance().getReference();
        username = binding.userName.getText().toString();
        email = binding.userEmail.getText().toString().trim();
        password = binding.password.getText().toString().trim();
        confirmPassword = binding.confirmPassword.getText().toString().trim();

        if(!username.equals("") && !email.equals("") && !password.equals("") && !confirmPassword.equals("")){
            if(password.equals(confirmPassword)){
                if(password.length() > 7){
                    //Sing In
                    CreateUser();
                }else {
                    Toast.makeText(this, R.string.must_8_char, Toast.LENGTH_SHORT).show();
                }
                
            }else{
                Toast.makeText(this, R.string.dont_macth_password, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, R.string.fill_requirements, Toast.LENGTH_LONG).show();
        }
    }

    private void clearText(){
        binding.userName.setText("");
        binding.userEmail.setText("");
        binding.password.setText("");
        binding.confirmPassword.setText("");
    }

    public void singUpTextClicked(View view){
        startActivity(new Intent(SignUp.this, SignIn.class));
    }

    public void CreateUser(){

        User user = new User(username,email,password);

        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                firebaseUser = auth.getCurrentUser();

                reference.child("User").child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        auth.signOut();
                        clearText();
                        startActivity(new Intent(SignUp.this, SignIn.class));
                        Toast.makeText(SignUp.this, R.string.successful, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {@Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignUp.this, R.string.fail, Toast.LENGTH_SHORT).show();
                }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUp.this, R.string.fail, Toast.LENGTH_SHORT).show();
            }
        });


    }
}