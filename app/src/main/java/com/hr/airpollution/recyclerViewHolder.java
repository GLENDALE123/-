package com.hr.airpollution;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class recyclerViewHolder extends RecyclerView.ViewHolder {
    public TextView mItemadress, mItempoultt;
    public ImageView mItempoulic;

    public recyclerViewHolder(View itemView) {
        super(itemView);
        mItemadress = (TextView) itemView.findViewById(R.id.item_address);
        mItempoultt = (TextView) itemView.findViewById(R.id.item_poultt);
        mItempoulic = (ImageView) itemView.findViewById(R.id.item_poulic);
    }
}
