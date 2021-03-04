package com.lockdownhelp.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DataProcessor dataProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth=FirebaseAuth.getInstance();

        dataProcessor= DataProcessor.getInstance(this);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAppPermissions();
            }
        },2000);
    }


    private void checkAppPermissions(){
        if ((ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)||(ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            showPermissionExplanation();
        } else {
            checkAuth();
        }
    }

    private void showPermissionExplanation(){
        Intent intent=new Intent(SplashScreen.this,AppPermissions.class);
        startActivity(intent);
        finish();
    }

    private void checkAuth(){
        if (mAuth.getCurrentUser()!=null){

            if (TextUtils.equals(dataProcessor.getHomeLocation(),"None")){
                Intent intent=new Intent(SplashScreen.this,MapActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent=new Intent(SplashScreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }


        }else{
            Intent intent=new Intent(SplashScreen.this,Login.class);
            startActivity(intent);
            finish();
        }

    }
}
