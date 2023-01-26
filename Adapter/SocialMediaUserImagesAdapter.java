package com.mustafa.message_app.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.mustafa.message_app.Models.SocialMediaUserImage;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.RecyclerSocialUserImageRowBinding;

import java.util.ArrayList;

public class SocialMediaUserImagesAdapter extends RecyclerView.Adapter<SocialMediaUserImagesAdapter.ViewHolder> {

    private ArrayList<SocialMediaUserImage> arrayList;
    private Context context;

    public SocialMediaUserImagesAdapter(ArrayList<SocialMediaUserImage> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SocialMediaUserImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerSocialUserImageRowBinding recyclerSocialUserImageRowBinding = RecyclerSocialUserImageRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(recyclerSocialUserImageRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SocialMediaUserImagesAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .load(arrayList.get(position).socialMediaImageUrl)
                .into(holder.binding.userImages);

        holder.itemView.setOnClickListener(item -> {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.show_user_profile_layout);

            ImageView closeLayout = dialog.findViewById(R.id.showUserProfileCloseLayoutImage);
            ImageView showUserProfile = dialog.findViewById(R.id.showUserProfileImageView);
            TextView failText = dialog.findViewById(R.id.failTextView);

            if(arrayList.get(position).socialMediaImageUrl != null){
                Glide.with(context)
                        .load(arrayList.get(position).socialMediaImageUrl)
                        .error(R.drawable.profile)
                        .into(showUserProfile);
            }else{
                failText.setVisibility(View.VISIBLE);
            }

            closeLayout.setOnClickListener(item2 ->{
                dialog.dismiss();
            });

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerSocialUserImageRowBinding binding;
        public ViewHolder(RecyclerSocialUserImageRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
