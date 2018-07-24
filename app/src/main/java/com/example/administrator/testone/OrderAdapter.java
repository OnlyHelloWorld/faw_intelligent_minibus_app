package com.example.administrator.testone;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2018/7/23.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context mContext;
    private List<Order> mOrderList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView starTime;
        TextView startPoint;
        TextView endPoint;
        TextView state;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            starTime = (TextView) view.findViewById(R.id.startT);
            startPoint = (TextView) view.findViewById(R.id.startP);
            endPoint = (TextView) view.findViewById(R.id.endP);
            state = (TextView) view.findViewById(R.id.state);
        }
    }
        public OrderAdapter(List<Order> orderList){
            mOrderList=orderList;
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext=parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.order_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order order=mOrderList.get(position);
        holder.starTime.setText(order.getBeginTime());
        holder.startPoint.setText(order.getEndPoint());
        holder.endPoint.setText(order.getEndPoint());
        holder.state.setText(order.getOrderStation());
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }
}
