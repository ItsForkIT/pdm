package com.disarm.surakshit.pdm.Fragments;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by naman on 25/2/18.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {
    FragmentManager fm;
    public FragmentAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            if(fm.getFragments().size()!=0)
                return fm.getFragments().get(0);
            ChatFragment chat = new ChatFragment();
            return chat;
        }
        else{
            if(fm.getFragments().size()!=0)
                return fm.getFragments().get(1);
            return new MapFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
