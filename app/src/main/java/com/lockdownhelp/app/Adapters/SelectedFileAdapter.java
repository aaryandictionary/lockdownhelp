package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.SelectedImageModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class SelectedFileAdapter extends RecyclerView.Adapter<SelectedFileAdapter.SelectedFileViewHolder> {

    Context mCxt;
    List<SelectedImageModel> selectedImageModelList;
    OnSelectedFileEvents onSelectedFileEvents;
    String callType;

    public SelectedFileAdapter(Context mCxt, List<SelectedImageModel> selectedImageModelList, OnSelectedFileEvents onSelectedFileEvents, String callType) {
        this.mCxt = mCxt;
        this.selectedImageModelList = selectedImageModelList;
        this.onSelectedFileEvents = onSelectedFileEvents;
        this.callType = callType;
    }

    @NonNull
    @Override
    public SelectedFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.selected_image_list_item_layout,parent,false);
        return new SelectedFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedFileViewHolder holder, int position) {
        final SelectedImageModel selectedImageModel=selectedImageModelList.get(position);
        holder.txtSize.setText((selectedImageModel.getFileSize()/1024)+" MB");
        holder.txtCategoryName.setText(selectedImageModel.getFileName());
        Glide.with(mCxt).load(selectedImageModel.getFileUrl()).into(holder.imgSelected);
        holder.imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedFileEvents.onRemoveBtnClick(selectedImageModel);
            }
        });
        if (TextUtils.equals(callType,"Form")){
            holder.imgBtnDelete.setVisibility(View.VISIBLE);
        }else{
            holder.imgBtnDelete.setVisibility(View.GONE);
        }

        holder.relMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedFileEvents.onFileListClick(selectedImageModel.getFileUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedImageModelList.size();
    }

    public class SelectedFileViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout relMain;
        ImageView imgSelected;
        TextView txtCategoryName,txtSize;
        ImageButton imgBtnDelete;

        public SelectedFileViewHolder(@NonNull View itemView) {
            super(itemView);
            relMain=itemView.findViewById(R.id.relMain);
            imgSelected=itemView.findViewById(R.id.imgSelected);
            txtCategoryName=itemView.findViewById(R.id.txtCategoryName);
            txtSize=itemView.findViewById(R.id.txtSize);
            imgBtnDelete=itemView.findViewById(R.id.imgBtnDelete);
        }
    }

    public interface OnSelectedFileEvents{
       void onFileListClick(String fileUrl);
       void onRemoveBtnClick(SelectedImageModel selectedImageModel);
    }
}
