package com.lockdownhelp.app.Utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lockdownhelp.app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RequestBottomSheetType extends BottomSheetDialogFragment {

    private RequestBottomSheetListener mListener;
    DataProcessor dataProcessor;

    int maxCount,counter=1;

    String subCatTitle,categoryMessage,bottomSheetType,categoryTitle;

    TextView txtMessage,txtMyLocation,txtChangeLocation,txtTitle,tN,txtCount;
    Button btnSendRequest;
    ImageButton imgBtnPlus,imgBtnMinus;

    RelativeLayout relSpinner,relContainer;
    LinearLayout linSelector;

    Spinner spinner_type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.bottom_sheet_input_type_1,container,false);



        dataProcessor=DataProcessor.getInstance(getContext());

        txtMessage=view.findViewById(R.id.txtMessage);
        txtMyLocation=view.findViewById(R.id.txtMyLocation);
        txtChangeLocation=view.findViewById(R.id.txtChangeLocation);
        btnSendRequest=view.findViewById(R.id.btnSendRequest);
        txtTitle=view.findViewById(R.id.txtTitle);
        tN=view.findViewById(R.id.tN);
        txtCount=view.findViewById(R.id.txtCount);
        imgBtnPlus=view.findViewById(R.id.imgBtnPlus);
        imgBtnMinus=view.findViewById(R.id.imgBtnMinus);
        relSpinner=view.findViewById(R.id.relSpinner);
        linSelector=view.findViewById(R.id.linSelector);
        relContainer=view.findViewById(R.id.relContainer);
        spinner_type=view.findViewById(R.id.spinner_type);

        subCatTitle = this.getArguments().getString("subCategoryId");
        categoryMessage = this.getArguments().getString("categoryMessage");
        bottomSheetType = this.getArguments().getString("bottomSheetType");
        categoryTitle = this.getArguments().getString("categoryTitle");
        if (TextUtils.equals(bottomSheetType,"BottomSheet")){
            maxCount = this.getArguments().getInt("maxCount");
            txtCount.setText("1");
            if (TextUtils.equals(categoryTitle,"Shelter")){
                txtTitle.setText("Select Number of People");
                tN.setText("Number of People");
            }else{
                txtTitle.setText("Select Number of "+subCatTitle);
                tN.setText("Number of "+subCatTitle);
            }

            relSpinner.setVisibility(View.GONE);
            linSelector.setVisibility(View.VISIBLE);
        }else if (TextUtils.equals(bottomSheetType,"BottomSheetSpinner")){
            relSpinner.setVisibility(View.VISIBLE);
            linSelector.setVisibility(View.GONE);
            txtTitle.setText("Select the reason for request");
            mListener.loadSpinner(subCatTitle,spinner_type);
        }else if (TextUtils.equals(bottomSheetType,"BottomSheetNone")){
            relSpinner.setVisibility(View.GONE);
            linSelector.setVisibility(View.GONE);
            txtTitle.setText("Confirm the request");
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)txtMessage.getLayoutParams();
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)relContainer.getLayoutParams();
            params.setMargins(20, 10, 20, 0);
            params1.setMargins(20, 0, 20, 0);
            txtMessage.setLayoutParams(params);
            relContainer.setLayoutParams(params1);
        }

        txtMyLocation.setText(dataProcessor.getHomeLocation());


        txtMessage.setText(categoryMessage);

        txtChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onChangeLocationClick();
            }
        });

        imgBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCounter("Plus");
            }
        });

        imgBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCounter("Minus");
            }
        });

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSendRequestClick(bottomSheetType,txtCount.getText().toString(),"");

            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        txtMyLocation.setText(dataProcessor.getHomeLocation());
    }

    public interface  RequestBottomSheetListener{
        void onChangeLocationClick();
        void onSendRequestClick(String type,String numOfPackets,String reason);
        void loadSpinner(String subCategoryId, Spinner spinner);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener=(RequestBottomSheetType.RequestBottomSheetListener)context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+ "must implement BottomSheetListener");
        }
    }

    private void changeCounter(String type){
        if (TextUtils.equals(type,"Plus")){
            if (counter<maxCount){
                counter=counter+1;
                txtCount.setText(String.valueOf(counter));
            }else {
                Toast.makeText(getContext(),"Maximum limit reached",Toast.LENGTH_SHORT).show();
            }
        }else{
            if (counter>1){
                counter=counter-1;
                txtCount.setText(String.valueOf(counter));
            }
        }
    }
}

