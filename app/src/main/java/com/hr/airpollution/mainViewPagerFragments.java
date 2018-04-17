package com.hr.airpollution;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class mainViewPagerFragments extends Fragment {
    private TextView mtextview;
    private ImageView mimageview;
    List<Integer> list;
    JSONObject tempJSONObject;

    public mainViewPagerFragments(JSONObject tempJSONObject) {
        this.tempJSONObject = tempJSONObject;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mainviewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mtextview = (TextView) view.findViewById(R.id.poultt);
        mimageview = (ImageView) view.findViewById(R.id.poulic);


        try {
            mtextview.setText(tempJSONObject.get("text").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}