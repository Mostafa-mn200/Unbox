package com.example.Unbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.Unbox.fragments.chatFragment;
import com.example.Unbox.fragments.contactsFragment;
import com.example.Unbox.Group.groupFragment;

public class TabsAccesstorAdapter extends FragmentPagerAdapter {
    public TabsAccesstorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                chatFragment chatFragment=new chatFragment();
                return chatFragment;
            case 1:
                groupFragment groupFragment=new groupFragment();
                return groupFragment;
            case 2:
                contactsFragment contactsFragment=new contactsFragment();
                return contactsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }
}
