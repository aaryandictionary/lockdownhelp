package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.lockdownhelp.app.Models.ViewPagerModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<ViewPagerModel>viewPagerModels;
    private LayoutInflater layoutInflater;
    private Context context;
    ViewPagerEvents viewPagerEvents;
    ChipGroup chipGroup;
    int position;
    TextView txtQuestion;
    EditText textResponse;
    ImageView imgQuestion;

    Button btnSubmit;

    public ViewPagerAdapter(List<ViewPagerModel> viewPagerModels, Context context, ViewPagerEvents viewPagerEvents) {
        this.viewPagerModels = viewPagerModels;
        this.context = context;
        this.viewPagerEvents = viewPagerEvents;
    }

    @Override
    public int getCount() {
        return viewPagerModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        if (!TextUtils.equals(viewPagerModels.get(position).getSelectionType(),""))
        viewPagerEvents.onListPopulate(chipGroup,viewPagerModels.get(position).getQuestion(),viewPagerModels.get(position).getSelectionType(),viewPagerModels.get(position).getResponses().size(),textResponse);
        //Toast.makeText(context,"is view from obj",Toast.LENGTH_SHORT).show();
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=LayoutInflater.from(context);
        View view;
        if (!TextUtils.equals(viewPagerModels.get(position).getSelectionType(),"")){
            view=layoutInflater.inflate(R.layout.covid_question_item_layout,container,false);

            this.position=position;

            imgQuestion=view.findViewById(R.id.imgQuestion);
            txtQuestion=view.findViewById(R.id.txtQuestion);

            chipGroup=view.findViewById(R.id.chipGroup);
            textResponse=view.findViewById(R.id.textResponse);
        }else{
            view=layoutInflater.inflate(R.layout.covid_question_submit_layout,container,false);
            imgQuestion=view.findViewById(R.id.imgQuestion);
            txtQuestion=view.findViewById(R.id.txtQuestion);
            btnSubmit=view.findViewById(R.id.btnSumit);
        }


        if (!TextUtils.equals(viewPagerModels.get(position).getSelectionType(),"")){
            Glide.with(context).load(viewPagerModels.get(position).getQuestionIconUrl()).into(imgQuestion);
            txtQuestion.setText(viewPagerModels.get(position).getQuestion());
            if (TextUtils.equals(viewPagerModels.get(position).getSelectionType(),"Single")||TextUtils.equals(viewPagerModels.get(position).getSelectionType()
                    ,"Multiple")){
                addToGroup(viewPagerModels.get(position).getResponses(),chipGroup,position);
                chipGroup.setVisibility(View.VISIBLE);
                textResponse.setVisibility(View.GONE);
            }else{
                if (TextUtils.equals(viewPagerModels.get(position).getSelectionType(),"EditNum")){
                    textResponse.setInputType(InputType.TYPE_CLASS_PHONE);
                }else{
                    textResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                setDataToEditText(viewPagerModels.get(position).getResponses(),position,viewPagerModels.get(position).getQuestion(),viewPagerModels.get(position).getSelectionType());
                textResponse.setVisibility(View.VISIBLE);
                chipGroup.setVisibility(View.GONE);
            }
        }else{
            Glide.with(context).load(viewPagerModels.get(position).getQuestionIconUrl()).into(imgQuestion);
            txtQuestion.setText(viewPagerModels.get(position).getQuestion());
           // viewPagerEvents.loadSubmitData(imgQuestion,txtQuestion);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPagerEvents.onSubmitClick();
                }
            });
        }


        container.addView(view,0);


        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    private void setDataToEditText(List<String>tagList, final int position, final String question, final String selectionType){
        for (int i=0;i<tagList.size();i++){
            String name=tagList.get(i);
            textResponse.setHint(name);
        }

        textResponse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewPagerEvents.onTextChange(s.toString(),question,selectionType);
            }
        });
    }

    private void addToGroup(List<String>tagList, ChipGroup chipGroup, final int position){
        chipGroup.setSelectionRequired(true);
        if (TextUtils.equals(viewPagerModels.get(position).getSelectionType(),"Single")){
            chipGroup.setSingleSelection(true);
        }else{
            chipGroup.setSingleSelection(false);
        }

        for (int i=0;i<tagList.size();i++){
            final String name=tagList.get(i);
            final Chip chip = new Chip(context);
            chip.setChipDrawable(ChipDrawable.createFromAttributes(context,null, 0, R.style.CustomChipChoice));
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 40,
                    context.getResources().getDisplayMetrics()
            );
            chip.setPadding(paddingDp, 0, paddingDp, 0);
            //chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
            chip.setText(name);
            chip.setCloseIconEnabled(false);

            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    viewPagerEvents.onItemChecked(buttonView,isChecked,viewPagerModels.get(position).getQuestion(),viewPagerModels.get(position).getSelectionType(),txtQuestion);
                }
            });

            chipGroup.addView(chip);
        }
    }

    public interface ViewPagerEvents{
        void onItemChecked(CompoundButton buttonView, boolean isChecked,String question,String selectionType,TextView textView);
        void onListPopulate(ChipGroup chipGroup,String question,String selectionType,int size,EditText textResponse);
        void onTextChange(String text,String question,String selectionType);
        void loadSubmitData(ImageView imageView,TextView textView);
        void onSubmitClick();
    }
}
