package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.firebase.auth.FirebaseAuth;

public class AppPermissions extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Button btnIAgree;
    private FirebaseAuth mAuth;
    DataProcessor dataProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permissions);
        btnIAgree=findViewById(R.id.btnIAgree);

        mAuth=FirebaseAuth.getInstance();

        dataProcessor= DataProcessor.getInstance(this);

        btnIAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAppPremissions();
            }
        });

    }

    private void requestAppPremissions(){
        ActivityCompat.requestPermissions(AppPermissions.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAuth();
                }
            }

        }
    }


    private void checkAuth(){
        if (mAuth.getCurrentUser()!=null){

            if (TextUtils.equals(dataProcessor.getHomeLocation(),"None")){
                Intent intent=new Intent(AppPermissions.this,MapActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent=new Intent(AppPermissions.this,MainActivity.class);
                startActivity(intent);
                finish();
            }


        }else{
            Intent intent=new Intent(AppPermissions.this,Login.class);
            startActivity(intent);
            finish();
        }

    }
}
