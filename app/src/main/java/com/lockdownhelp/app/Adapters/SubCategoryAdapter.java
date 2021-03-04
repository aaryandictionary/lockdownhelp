package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.SubCategoryModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder> {

    Context mCxt;
    List<SubCategoryModel>subCategoryModels;
    SubCategoryEvents subCategoryEvents;

    public SubCategoryAdapter(Context mCxt, List<SubCategoryModel> subCategoryModels, SubCategoryEvents subCategoryEvents) {
        this.mCxt = mCxt;
        this.subCategoryModels = subCategoryModels;
        this.subCategoryEvents = subCategoryEvents;
    }

    @NonNull
    @Override
    public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.req_items_list_item_layout,parent,false);
        return new SubCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubCategoryViewHolder holder, int position) {
        final SubCategoryModel subCategoryModel=subCategoryModels.get(position);
        holder.txtTitle.setText(subCategoryModel.getSubCategoryName());
        Glide.with(mCxt).load(subCategoryModel.getSubCategoryIconUrl()).into(holder.imgReq);
        holder.relAddToCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCategoryEvents.onAddToCartClick(subCategoryModel.getSubCategoryName(),holder.relAddToCard,holder.linCounter,holder.txtCount,subCategoryModel.getMinCounter(),subCategoryModel.getSubCategoryUnit());
            }
        });

        holder.imgBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCategoryEvents.onPlusMinusClick(subCategoryModel.getSubCategoryName(),"Minus",subCategoryModel.getMaxCounter(),subCategoryModel.getMinCounter(),subCategoryModel.getMultipleCounter(),holder.relAddToCard,holder.linCounter,holder.txtCount,subCategoryModel.getSubCategoryUnit());
            }
        });

        holder.imgBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCategoryEvents.onPlusMinusClick(subCategoryModel.getSubCategoryName(),"Plus",subCategoryModel.getMaxCounter(),subCategoryModel.getMinCounter(),subCategoryModel.getMultipleCounter(),holder.relAddToCard,holder.linCounter,holder.txtCount,subCategoryModel.getSubCategoryUnit());
            }
        });
    }

    @Override
    public int getItemCount() {
        return subCategoryModels.size();
    }

    public class SubCategoryViewHolder extends RecyclerView.ViewHolder{
        ImageView imgReq;
        TextView txtTitle;
        RelativeLayout relAddToCard;
        RelativeLayout linCounter;
        ImageButton imgBtnMinus,imgBtnPlus;
        TextView txtCount;

        public SubCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReq=itemView.findViewById(R.id.imgReq);
            txtTitle=itemView.findViewById(R.id.txtTitle);
            relAddToCard=itemView.findViewById(R.id.relAddToCard);
            linCounter=itemView.findViewById(R.id.linCounter);
            imgBtnMinus=itemView.findViewById(R.id.imgBtnMinus);
            imgBtnPlus=itemView.findViewById(R.id.imgBtnPlus);
            txtCount=itemView.findViewById(R.id.txtCount);
        }
    }

    public interface SubCategoryEvents{
        void onAddToCartClick(String subCategoryTitle,RelativeLayout relativeLayout,RelativeLayout linearLayout,TextView textView,double minCounter,String unit);
        void onPlusMinusClick(String subCategoryTitle,String typeClick,double maxCounter,double minCounter,double multipleCounter,RelativeLayout relativeLayout,RelativeLayout linearLayout,TextView textView,String unit);
    }
}
