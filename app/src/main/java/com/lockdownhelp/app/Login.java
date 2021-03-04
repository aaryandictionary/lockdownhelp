package com.lockdownhelp.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.LoginSliderAdapter;
import com.lockdownhelp.app.Models.LoginSliderModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.BottomSheetOtpDialog;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity implements BottomSheetOtpDialog.BottomSheetListener {

    Button btnSendOtp;
    EditText textMobile;
    BottomSheetOtpDialog bottomSheetOtpDialog;

    SliderView sliderView;
    LoginSliderAdapter loginSliderAdapter;
    List<LoginSliderModel> loginSliderModels;

    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnSendOtp=findViewById(R.id.btnSendOtp);
        textMobile=findViewById(R.id.textMobile);

        sliderView=findViewById(R.id.imageSlider);

        loginSliderModels=new ArrayList<>();

        getSlider();

        textMobile.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number=textMobile.getText().toString().replaceAll("[\\s\\-()+.]","");
                if (!number.isEmpty()&& (number.length()==10)){
                    bottomSheetOtpDialog=new BottomSheetOtpDialog();
                    bottomSheetOtpDialog.show(getSupportFragmentManager(),"otpDialog");
                    bottomSheetOtpDialog.setCancelable(false);
                    Bundle bundle=new Bundle();
                    bundle.putString("mobile",number);
                    bottomSheetOtpDialog.setArguments(bundle);
                }else{
                    Toast.makeText(Login.this,"Enter valid Mobile",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getSlider(){
        loginSliderModels.clear();
        loginSliderModels.add(new LoginSliderModel("Deliver all your needs",R.drawable.slider1));
        loginSliderModels.add(new LoginSliderModel("Contactless delivery",R.drawable.slider2));
        loginSliderModels.add(new LoginSliderModel("Request Essentials, Medicine and more being in house",R.drawable.slider3));
        loginSliderAdapter=new LoginSliderAdapter(Login.this,loginSliderModels);
        sliderView.setSliderAdapter(loginSliderAdapter);
        sliderView.startAutoCycle();
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
    }


    @Override
    public void onChangeClick() {
        bottomSheetOtpDialog.dismiss();
        textMobile.setText("");
        textMobile.requestFocus();
    }

}
