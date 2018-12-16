package com.disarm.surakshit.pdm;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Service.SyncService;

import static com.disarm.surakshit.pdm.MainActivity.syncServiceBound;
import static com.disarm.surakshit.pdm.MainActivity.syncServiceConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Context c = getContext();

        if (s.equals("mule_switch") || s.equals("disarmConnect")) {
            try {
                Intent i = new Intent(c, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (getActivity().getSupportFragmentManager().getFragments().size() != 0) {
                    for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                        if (fragment != null) {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        }
                    }
                }
                c.startActivity(i);
            } catch (Exception e) {

            }
        }


    }
}
