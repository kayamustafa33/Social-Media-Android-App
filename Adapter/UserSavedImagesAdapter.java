package com.mustafa.message_app.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mustafa.message_app.Models.CheckSavedImages;
import com.mustafa.message_app.Models.UserSavedImages;
import com.mustafa.message_app.R;
import com.mustafa.message_app.databinding.RecyclerSavedImageRowBinding;
import java.util.ArrayList;

public class UserSavedImagesAdapter extends RecyclerView.Adapter<UserSavedImagesAdapter.ViewHolder> {

    private ArrayList<UserSavedImages> arrayList;
    private Context context;

    public UserSavedImagesAdapter(ArrayList<UserSavedImages> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserSavedImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerSavedImageRowBinding binding = RecyclerSavedImageRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSavedImagesAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(arrayList.get(position).savedDownloadUrl).into(holder.binding.userImages);

        holder.itemView.setOnClickListener(item -> {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.show_user_profile_layout);

            ImageView closeLayout = dialog.findViewById(R.id.showUserProfileCloseLayoutImage);
            ImageView showUserProfile = dialog.findViewById(R.id.showUserProfileImageView);
            TextView failText = dialog.findViewById(R.id.failTextView);

            if(arrayList.get(position).savedDownloadUrl != null){
                Glide.with(context)
                        .load(arrayList.get(position).savedDownloadUrl)
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
        RecyclerSavedImageRowBinding binding;
        public ViewHolder(RecyclerSavedImageRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
