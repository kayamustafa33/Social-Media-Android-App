package com.mustafa.message_app.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mustafa.message_app.Fragments.MyPicturesFragment;
import com.mustafa.message_app.Fragments.MySavedPicturesFragment;

public class SocialMediaTabLayoutAdapter extends FragmentStateAdapter {


    public SocialMediaTabLayoutAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new MySavedPicturesFragment();
            case 0:
            default:
                return new MyPicturesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
