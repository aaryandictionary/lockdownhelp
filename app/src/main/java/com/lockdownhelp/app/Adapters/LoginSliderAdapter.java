package com.lockdownhelp.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lockdownhelp.app.Models.LoginSliderModel;
import com.lockdownhelp.app.R;
import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class LoginSliderAdapter extends SliderViewAdapter<LoginSliderAdapter.SliderAdapterVH> {

    private Context context;

    private List<LoginSliderModel> loginSliderModels;

    public LoginSliderAdapter(Context context, List<LoginSliderModel> loginSliderModels) {
        this.context = context;
        this.loginSliderModels = loginSliderModels;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.login_slider_item_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        LoginSliderModel loginSliderModel = loginSliderModels.get(position);
        viewHolder.txtSlider.setText(loginSliderModel.getTitle());
        Glide.with(context).load(loginSliderModel.getImgUrl()).into(viewHolder.imgSlider);
    }

    @Override
    public int getCount() {
        return loginSliderModels.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imgSlider;
        TextView txtSlider;
        public SliderAdapterVH(View itemView) {
            super(itemView);
            imgSlider=itemView.findViewById(R.id.imgSlider);
            txtSlider=itemView.findViewById(R.id.txtSlider);
            this.itemView = itemView;
        }
    }

}
