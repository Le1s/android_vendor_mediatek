package com.mediatek.connectivity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
  *
  * Provide basic override functions for TabListener.
  *
  */
public class CdsTabListener implements ActionBar.TabListener {
    private Fragment mFragment;

    CdsTabListener(Fragment fragment) {
        this.mFragment = fragment;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.replace(R.id.fragment_container, mFragment);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(mFragment);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
        // nothing done here
    }
}