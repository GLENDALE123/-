package com.hr.airpollution;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class recyclerViewAdepter extends RecyclerView.Adapter<recyclerViewHolder> {
    private List<JSONObject> data;
    JSONObject tempJSONObject;
    @NonNull
    @Override
    public recyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.local_list, parent, false);
        return new recyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerViewHolder holder, int position) {
        JSONObject items = data.get(position);
        try {
            holder.mItemadress.setText(tempJSONObject.get("address").toString());
            holder.mItempoultt.setText(tempJSONObject.get("text").toString());
            switch (Integer.parseInt(tempJSONObject.get("grade").toString())) {
                case 1: // 좋음
                    holder.mItempoulic.setImageResource(R.drawable.verygood_ic);
                    break;
                case 2:
                    holder.mItempoulic.setImageResource(R.drawable.good_ic);
                    break;
                case 3:
                    holder.mItempoulic.setImageResource(R.drawable.bed_ic);
                    break;
                case 4: // 아주 나쁨
                    holder.mItempoulic.setImageResource(R.drawable.verybed_ic);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
    @Override
    public int getItemCount() {
        return 0;
    }
}
