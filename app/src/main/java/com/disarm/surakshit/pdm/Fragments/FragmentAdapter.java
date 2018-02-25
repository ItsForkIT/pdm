package com.disarm.surakshit.pdm.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by naman on 25/2/18.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            ChatFragment chat = new ChatFragment();
            return chat;
        }
        else{
            return new MapFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }


}
