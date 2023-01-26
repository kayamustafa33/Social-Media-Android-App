package com.mustafa.message_app.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mustafa.message_app.Fragments.FriendRequestFragment;
import com.mustafa.message_app.Fragments.MessagesFragment;

public class TabLayoutAdapter extends FragmentStateAdapter {

    public TabLayoutAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new FriendRequestFragment();
            case 0:
            default:
                return new MessagesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
