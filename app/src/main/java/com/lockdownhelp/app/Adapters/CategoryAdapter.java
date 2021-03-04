package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.CategoryModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    Context mCxt;
    List<CategoryModel>categoryModels;
    CategoryEvents categoryEvents;
    String requirementsTitle;

    public CategoryAdapter(Context mCxt, List<CategoryModel> categoryModels, CategoryEvents categoryEvents, String requirementsTitle) {
        this.mCxt = mCxt;
        this.categoryModels = categoryModels;
        this.categoryEvents = categoryEvents;
        this.requirementsTitle = requirementsTitle;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.requirements_list_item_layout,parent,false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        final CategoryModel categoryModel=categoryModels.get(position);

        if (categoryModel!=null){
            holder.txtReqTitle.setText(categoryModel.getCategoryName());
            Glide.with(mCxt).load(categoryModel.getCategoryIconUrl()).into(holder.imgReq);
            holder.relCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryEvents.onCategoryClick(categoryModel.getSubCategoryId(),categoryModel.getCategoryViewType(),requirementsTitle,categoryModel.getCategoryName(),categoryModel.getCategoryMessage(),categoryModel.getMaxCount(),categoryModel.getCategoryIconUrl());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        ImageView imgReq;
        TextView txtReqTitle;
        RelativeLayout relCategory;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReq=itemView.findViewById(R.id.imgReq);
            txtReqTitle=itemView.findViewById(R.id.txtReqTitle);
            relCategory=itemView.findViewById(R.id.relCategory);
        }
    }

    public interface CategoryEvents{
        void onCategoryClick(String subCategoryId,String categoryViewType,String requirementsTitle,String categoryTitle,String categoryMessage,int maxCount,String categoryIconUrl);
    }
}
