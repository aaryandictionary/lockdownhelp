package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lockdownhelp.app.Models.User;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.BottomSheetOtpDialog;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity implements BottomSheetOtpDialog.BottomSheetListener{
    TextInputLayout txtName,txtAddress,txtMobile;
    DataProcessor dataProcessor;

    ImageButton imgBtnDone,btnBackSubCategory;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        txtName=findViewById(R.id.txtName);
        txtAddress=findViewById(R.id.txtAddress);
        txtMobile=findViewById(R.id.txtMobile);


        imgBtnDone=findViewById(R.id.imgBtnDone);
        btnBackSubCategory=findViewById(R.id.btnBackSubCategory);

        dataProcessor=DataProcessor.getInstance(this);
        mAuth=FirebaseAuth.getInstance();

        if (!TextUtils.equals(dataProcessor.getUserName(),"None")){
            txtName.getEditText().setText(dataProcessor.getUserName());
        }

        if (!TextUtils.equals(dataProcessor.getUserAddress(),"None")){
            txtAddress.getEditText().setText(dataProcessor.getUserAddress());
        }

        if (!TextUtils.equals(dataProcessor.getUserMobile(),"None")){
            txtMobile.getEditText().setText(dataProcessor.getUserMobile());
        }

        imgBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(txtName.getEditText().getText().toString().trim()) && !TextUtils.isEmpty(txtAddress.getEditText().getText().toString().trim())){

                    User user=new User();
                    user.setUserId(mAuth.getCurrentUser().getUid());
                    user.setUserName(txtName.getEditText().getText().toString());
                    saveUser(user);
                }
            }
        });

        btnBackSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfile.this, MapActivity.class);
                intent.putExtra("openType","EditProfile");
                startActivity(intent);
            }
        });

        txtAddress.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfile.this, MapActivity.class);
                intent.putExtra("openType","EditProfile");
                startActivity(intent);
            }
        });
    }


    private void saveUser(final User user){
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("Users");

        Map<String,Object> map=new HashMap<>();
        map.put("userName",user.getUserName());



        if (user!=null){
            dbUser.child(user.getUserId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    makeInitials(user.getUserName());
                    onBackPressed();
                    Toast.makeText(EditProfile.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtAddress.getEditText().setText(dataProcessor.getHomeLocation());
    }

    @Override
    public void onChangeClick() {

    }


    private void makeInitials(String name){
        String x = name;
        String[] nameparts = x.split(" ");
        if (nameparts.length>1){
            if (Character.isAlphabetic(nameparts[0].charAt(0))&&Character.isAlphabetic(nameparts[0].charAt(0))){
                char fName = Character.toUpperCase(nameparts[0].charAt(0));
                char lName = Character.toUpperCase(nameparts[1].charAt(0));
                dataProcessor.setInitials(fName+""+lName);
            }
        }else if (nameparts.length==1){
            if (Character.isAlphabetic(nameparts[0].charAt(0))){
                char fName = Character.toUpperCase(nameparts[0].charAt(0));
                dataProcessor.setInitials(String.valueOf(fName));
            }
        }
        dataProcessor.setUserName(name);
    }


}
