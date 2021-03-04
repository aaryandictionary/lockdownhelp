package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.lockdownhelp.app.Models.ViewPagerModel;
import com.lockdownhelp.app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class QuestionsHistoryAdapter extends PagerAdapter {

    private List<ViewPagerModel> viewPagerModels;
    private LayoutInflater layoutInflater;
    private Context context;

    TextView txtQuestion;
    ChipGroup chipGroup;

    public QuestionsHistoryAdapter(List<ViewPagerModel> viewPagerModels, Context context) {
        this.viewPagerModels = viewPagerModels;
        this.context = context;
    }

    @Override
    public int getCount() {
        return viewPagerModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.covid_question_history_layout,container,false);

        txtQuestion=view.findViewById(R.id.txtQuestion);
        chipGroup=view.findViewById(R.id.chipGroup);

        txtQuestion.setText(viewPagerModels.get(position).getQuestion());
        addChips(viewPagerModels.get(position).getResponses());

        container.addView(view,0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    private void addChips(List<String>responseList){

        chipGroup.setEnabled(false);
        chipGroup.setClickable(false);

        for (int i=0;i<responseList.size();i++){
            final String name=responseList.get(i);
            final Chip chip = new Chip(context);
            chip.setChipDrawable(ChipDrawable.createFromAttributes(context,null, 0, R.style.CustomChipChoice));
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 40,
                    context.getResources().getDisplayMetrics()
            );
            chip.setPadding(paddingDp, 0, paddingDp, 0);
            chip.setText(name);
            chip.setCloseIconEnabled(false);

            chipGroup.addView(chip);
        }
    }
}
