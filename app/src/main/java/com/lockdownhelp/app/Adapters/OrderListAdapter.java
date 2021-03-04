package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.RequestItemListType;
import com.lockdownhelp.app.R;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderListViewHolder> {

    Context mCxt;
    List<RequestItemListType>requestItemListTypes;

    public OrderListAdapter(Context mCxt, List<RequestItemListType> requestItemListTypes) {
        this.mCxt = mCxt;
        this.requestItemListTypes = requestItemListTypes;
    }

    @NonNull
    @Override
    public OrderListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.order_item_list_layout,parent,false);
        return new OrderListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderListViewHolder holder, int position) {
        RequestItemListType requestItemListType=requestItemListTypes.get(position);
        holder.txtSNo.setText(String.valueOf(position+1));
        holder.txtItem.setText(requestItemListType.getItemName());
        holder.txtQuantity.setText(requestItemListType.getItemQuantity()+" "+requestItemListType.getItemUnit());
    }

    @Override
    public int getItemCount() {
        return requestItemListTypes.size();
    }

    public class OrderListViewHolder extends RecyclerView.ViewHolder{
        TextView txtSNo,txtItem,txtQuantity;
        public OrderListViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSNo=itemView.findViewById(R.id.txtSNo);
            txtItem=itemView.findViewById(R.id.txtItem);
            txtQuantity=itemView.findViewById(R.id.txtQuantity);
        }
    }
}
