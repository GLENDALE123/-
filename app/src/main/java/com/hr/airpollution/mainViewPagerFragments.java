package com.hr.airpollution;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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

@SuppressLint("ValidFragment")
public class mainViewPagerFragments extends Fragment {
    private TextView mtextview;
    private TextView addressTextView;
    private TextView timeTextView;
    private ImageView mimageview;
    private ConstraintLayout container;
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
        container = (ConstraintLayout) view.findViewById(R.id.container);
        addressTextView = (TextView) view.findViewById(R.id.adress);
        timeTextView = (TextView) view.findViewById(R.id.updatetime);

        try {
            addressTextView.setText(tempJSONObject.get("address").toString());
            mtextview.setText(tempJSONObject.get("text").toString());
            timeTextView.setText(tempJSONObject.get("currentDateTimeString").toString());

            switch (Integer.parseInt(tempJSONObject.get("grade").toString())) {
                case 1: // 좋음
                    mimageview.setImageResource(R.drawable.verygood_ic);
                    container.setBackgroundResource(R.drawable.verygood_bg);
                    break;
                case 2:
                    mimageview.setImageResource(R.drawable.good_ic);
                    container.setBackgroundResource(R.drawable.good_bg);
                    break;
                case 3:
                    mimageview.setImageResource(R.drawable.bed_ic);
                    container.setBackgroundResource(R.drawable.bed_bg);
                    break;
                case 4: // 아주 나쁨
                    mimageview.setImageResource(R.drawable.verybed_ic);
                    container.setBackgroundResource(R.drawable.verybed_bg);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}