package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.MyRequestsModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.MyRequestsViewHolder> {

    Context mCxt;
    List<MyRequestsModel> myRequestsModelList;
    OnRequestClickEvents onRequestClickEvents;

    public MyRequestsAdapter(Context mCxt, List<MyRequestsModel> myRequestsModelList, OnRequestClickEvents onRequestClickEvents) {
        this.mCxt = mCxt;
        this.myRequestsModelList = myRequestsModelList;
        this.onRequestClickEvents = onRequestClickEvents;
    }

    @NonNull
    @Override
    public MyRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.my_requests_list_item_layout,parent,false);
        return new MyRequestsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRequestsViewHolder holder, int position) {
        final MyRequestsModel myRequestsModel=myRequestsModelList.get(position);
        Glide.with(mCxt).load(myRequestsModel.getCategoryIconUrl()).into(holder.imgCategory);
        holder.txtCategoryName.setText(myRequestsModel.getRequestTitle());
        if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Delivery")){
            holder.txtRequestStatus.setText("Request Processing");
        }else{
            holder.txtRequestStatus.setText("Request "+myRequestsModel.getRequestStatus());
        }
        holder.txtDate.setText(getDate(myRequestsModel.getRequestTimeStamp(),"dd MMMM 'at' hh:mm a"));
        holder.relMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestClickEvents.onMyRequestClick(myRequestsModel.getRequestId(),myRequestsModel.getRequestViewType());
            }
        });

        if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Success")){
            holder.txtRequestStatus.setTextColor(mCxt.getResources().getColor(R.color.colorLightBlue));
        }else if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Accepted")){
            holder.txtRequestStatus.setTextColor(mCxt.getResources().getColor(R.color.colorYellow));
        }else if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Rejected")){
            holder.txtRequestStatus.setTextColor(mCxt.getResources().getColor(R.color.colorRed));
        }else if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Delivery")){
            holder.txtRequestStatus.setTextColor(mCxt.getResources().getColor(R.color.quantum_amber700));
        }else if (TextUtils.equals(myRequestsModel.getRequestStatus(),"Completed")){
            holder.txtRequestStatus.setTextColor(mCxt.getResources().getColor(R.color.colorGreen));
        }
    }

    @Override
    public int getItemCount() {
        return myRequestsModelList.size();
    }

    public class MyRequestsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imgCategory;
        TextView txtCategoryName,txtDate,txtRequestStatus;
        RelativeLayout relMain;
        public MyRequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory=itemView.findViewById(R.id.imgCategory);
            txtCategoryName=itemView.findViewById(R.id.txtCategoryName);
            txtDate=itemView.findViewById(R.id.txtDate);
            txtRequestStatus=itemView.findViewById(R.id.txtRequestStatus);
            relMain=itemView.findViewById(R.id.relMain);
        }
    }

    public interface OnRequestClickEvents{
        void onMyRequestClick(String requestId,String requestViewType);
    }

    public static String getDate(long milliSeconds, String dateFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(new Date(milliSeconds));
    }
}
