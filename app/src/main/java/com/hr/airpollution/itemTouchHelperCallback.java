package com.hr.airpollution;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class itemTouchHelperCallback extends ItemTouchHelper.Callback {
    public interface  OnItemMoveListener {
        boolean onItemMove(int fromPosition,int toPosition);
    }
    private final OnItemMoveListener mItemMoveListener;
    public itemTouchHelperCallback(RecyclerView.OnItemTouchListener listener) {
        mItemMoveListener = (OnItemMoveListener) listener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags,swipeFlags);
    }
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mItemMoveListener.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return false;

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
