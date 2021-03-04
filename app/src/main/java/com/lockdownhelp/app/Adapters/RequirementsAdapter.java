package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Models.RequirementsModel;
import com.lockdownhelp.app.R;

import java.util.List;

public class RequirementsAdapter extends RecyclerView.Adapter<RequirementsAdapter.RequirementsViewHolder> {

    Context mCxt;
    List<RequirementsModel> requirementsModelList;
    RequirementEvents requirementEvents;

    public RequirementsAdapter(Context mCxt, List<RequirementsModel> requirementsModelList, RequirementEvents requirementEvents) {
        this.mCxt = mCxt;
        this.requirementsModelList = requirementsModelList;
        this.requirementEvents = requirementEvents;
    }

    @NonNull
    @Override
    public RequirementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mCxt).inflate(R.layout.requirements_category_list_item_layout,parent,false);
        return new RequirementsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequirementsViewHolder holder, int position) {
        RequirementsModel requirementsModel=requirementsModelList.get(position);
        if (requirementsModel!=null){
            holder.txtCategoryTitle.setText(requirementsModel.getRequirementTitle());
            requirementEvents.loadCategories(requirementsModel.getCategoryId(),holder.recycler_requirements,requirementsModel.getRequirementTitle());
        }
    }

    @Override
    public int getItemCount() {
        return requirementsModelList.size();
    }

    public class RequirementsViewHolder extends RecyclerView.ViewHolder{
        TextView txtCategoryTitle;
        RecyclerView recycler_requirements;
        public RequirementsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryTitle=itemView.findViewById(R.id.txtCategoryTitle);
            recycler_requirements=itemView.findViewById(R.id.recycler_requirements);
        }
    }

    public interface RequirementEvents{
        void loadCategories(String categoryId,RecyclerView categoryRecycler,String requirementsTitle);
    }
}
