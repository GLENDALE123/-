package com.hr.airpollution;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.json.JSONObject;

import java.util.List;

public class viewPagerAdepter extends FragmentStatePagerAdapter {
    private Context ctx;
    private List<JSONObject> data;
    private Fragment[] fragments;


    public viewPagerAdepter(Context ctx, FragmentManager fm, List<JSONObject> data) {
        super(fm);
        this.ctx = ctx;
        this.data = data;
        fragments = new Fragment[data.size()];
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        JSONObject items = data.get(position);


        mainViewPagerFragments mvpfragments = new mainViewPagerFragments(items);
        fragment = mvpfragments;

        if (fragments[position] == null) {
            fragments[position] = fragment;
        }
        return fragments[position];
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }
}
