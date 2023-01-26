package com.mustafa.message_app.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mustafa.message_app.Adapter.PostAdapter;
import com.mustafa.message_app.Models.Post;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.FragmentSocialMediaHomePageBinding;

import java.util.ArrayList;
import java.util.Map;

public class SocialMediaHomePageFragment extends Fragment {

    private FragmentSocialMediaHomePageBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private ArrayList<Post> postArrayList;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSocialMediaHomePageBinding.inflate(inflater,container,false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();


        postArrayList = new ArrayList<>();

        getData();

        binding.homePageRV.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        postAdapter = new PostAdapter(postArrayList,binding.getRoot().getContext());
        binding.homePageRV.setAdapter(postAdapter);


        return binding.getRoot();
    }

    private void getData(){

        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        String comment = String.valueOf(data.get("comment"));
                        int likes = Integer.parseInt( String.valueOf(data.get("likes")));

                        Post post = new Post(email,comment,downloadUrl,likes);
                        postArrayList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                }
            }
        });

    }
}