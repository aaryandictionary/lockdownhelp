package com.lockdownhelp.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfile extends AppCompatActivity {
    TextView txtEdit,txtName,txtMobile,txtNameInitials;
    RelativeLayout relYourRequests,relLogout,relHelpUs,relPrivacy;
    ImageButton imgBtnBack;

    ProgressDialog progressDialog;
    DataProcessor dataProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        txtEdit=findViewById(R.id.txtEdit);
        relYourRequests=findViewById(R.id.relYourRequests);
        imgBtnBack=findViewById(R.id.imgBtnBack);
        txtNameInitials=findViewById(R.id.txtNameInitials);

        txtName=findViewById(R.id.txtName);
        txtMobile=findViewById(R.id.txtMobile);
        relLogout=findViewById(R.id.relLogout);
        relHelpUs=findViewById(R.id.relHelpUs);
        relPrivacy=findViewById(R.id.relPrivacy);

        dataProcessor=DataProcessor.getInstance(this);
        progressDialog=new ProgressDialog(this);

        txtName.setText(dataProcessor.getUserName());
        txtMobile.setText(dataProcessor.getUserMobile());
        if (!TextUtils.equals(dataProcessor.getUserInitials(),"None")){
            txtNameInitials.setText(dataProcessor.getUserInitials());
        }

        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserProfile.this,EditProfile.class);
                startActivity(intent);
            }
        });

        relYourRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserProfile.this,MyRequests.class);
                startActivity(intent);
            }
        });
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        relLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert=new AlertDialog.Builder(UserProfile.this);
                alert.setMessage("Sure want to Logout");
                alert.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog.show();
                        FirebaseAuth.getInstance().signOut();
                        dataProcessor.deleteAll();
                        progressDialog.dismiss();
                        Intent intent=new Intent(UserProfile.this,Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog=alert.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();


            }
        });
        relPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
                startActivity(browserIntent);
            }
        });

        relHelpUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone="+"+919358174783"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtName.setText(dataProcessor.getUserName());
        txtNameInitials.setText(dataProcessor.getUserInitials());
    }
}
