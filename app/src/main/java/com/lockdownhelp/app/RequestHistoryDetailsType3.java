package com.lockdownhelp.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lockdownhelp.app.Adapters.SelectedFileAdapter;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.SelectedImageModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RequestHistoryDetailsType3 extends AppCompatActivity implements SelectedFileAdapter.OnSelectedFileEvents {

    RelativeLayout relHeader,relRequestStatus;
    ImageButton imgBtnBack;
    TextView txtRequestMessage,txtRequestTime,txtAfterRequestMessage,txtRequestStatus,txtFrom,txtTo,txtDistance;

    Button btnCancelRequest;
    long estimatedServerTimeMs=0;
    long  offset;
    DataProcessor dataProcessor;
    RecyclerView recycler_file;
    List<SelectedImageModel>selectedImageModels;
    SelectedFileAdapter selectedFileAdapter;

    ProgressDialog progressDialog;

    NestedScrollView mainNestedScrollView;
    AVLoadingIndicatorView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_history_details_type3);
        relHeader=findViewById(R.id.relHeader);
        relRequestStatus=findViewById(R.id.relRequestStatus);
        imgBtnBack=findViewById(R.id.imgBtnBack);
        txtRequestMessage=findViewById(R.id.txtRequestMessage);
        txtRequestTime=findViewById(R.id.txtRequestTime);
        txtAfterRequestMessage=findViewById(R.id.txtAfterRequestMessage);
        btnCancelRequest=findViewById(R.id.btnCancelRequest);
        txtRequestStatus=findViewById(R.id.txtRequestStatus);

        recycler_file=findViewById(R.id.recycler_file);
        recycler_file.setHasFixedSize(true);
        recycler_file.setLayoutManager(new LinearLayoutManager(this));

        txtFrom=findViewById(R.id.txtFrom);
        txtTo=findViewById(R.id.txtTo);
        txtDistance=findViewById(R.id.txtDistance);

        mainNestedScrollView=findViewById(R.id.mainNestedScrollView);
        progress=findViewById(R.id.progress);

        selectedImageModels=new ArrayList<>();
        progressDialog=new ProgressDialog(this);
        final String requestId=getIntent().getStringExtra("request_id");

        dataProcessor=DataProcessor.getInstance(this);


        loadData(requestId);

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Cancelling request...");
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelRequest(requestId);

                    }
                },1000);
            }
        });

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void cancelRequest(final String requestId){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Requests").child(requestId);

        newData();
        estimatedServerTimeMs = System.currentTimeMillis() + offset;

        Map<String,Object> map=new HashMap();
        map.put("requestStatus","Cancelled");
        map.put("requestStatusHeading","Request cancelled successfully");
        map.put("requestCancelTime",estimatedServerTimeMs);

        databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    relHeader.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    relRequestStatus.setVisibility(View.GONE);
                    btnCancelRequest.setVisibility(View.GONE);
                    txtRequestMessage.setText("Request cancelled successfully");
                    txtRequestTime.setText(getDate(estimatedServerTimeMs,"dd MMMM 'at' hh:mm a"));
                    progressDialog.dismiss();
                }else{
                    Toast.makeText(RequestHistoryDetailsType3.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }



    public void newData(){
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                offset = snapshot.getValue(Long.class);
                estimatedServerTimeMs = System.currentTimeMillis() + offset;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void loadData(final String requestId){
        mainNestedScrollView.setVisibility(View.GONE);
        progress.show();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Requests").child(requestId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Request request=dataSnapshot.getValue(Request.class);
                    txtRequestMessage.setText(request.getRequestStatusHeading());
                    txtAfterRequestMessage.setText(request.getRequestMessage());
                    txtRequestStatus.setText(request.getRequestStatusMessage());
                    selectedImageModels=request.getSelectedImageModels();

                    txtFrom.setText(request.getUserLocation());
                    txtTo.setText(request.getRequestLocationDetails().getAdminLocationDetails().getAddress());
                    DecimalFormat df = new DecimalFormat("####0.00");
                    txtDistance.setText(df.format(request.getRequestLocationDetails().getDistance())+" km");

                    txtRequestTime.setText(getDate(request.getRequestTimeStamp(),"dd MMMM 'at' hh:mm a"));

                    if (TextUtils.equals(request.getRequestStatus(),"Success")){
                        relHeader.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        relRequestStatus.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        btnCancelRequest.setVisibility(View.VISIBLE);
                    }else if (TextUtils.equals(request.getRequestStatus(),"Cancelled")){
                        relHeader.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        relRequestStatus.setVisibility(View.GONE);
                        btnCancelRequest.setVisibility(View.GONE);
                        txtRequestTime.setText(getDate(request.getRequestCancelTime(),"dd MMMM 'at' hh:mm a"));
                    }else if (TextUtils.equals(request.getRequestStatus(),"Accepted")){
                        relHeader.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        relRequestStatus.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                        btnCancelRequest.setVisibility(View.GONE);
                    }else if (TextUtils.equals(request.getRequestStatus(),"Rejected")){
                        relHeader.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        relRequestStatus.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        btnCancelRequest.setVisibility(View.GONE);
                    }

                    selectedFileAdapter=new SelectedFileAdapter(RequestHistoryDetailsType3.this,selectedImageModels,RequestHistoryDetailsType3.this,"History");
                    recycler_file.setAdapter(selectedFileAdapter);

                    progress.hide();
                    mainNestedScrollView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static String getDate(long milliSeconds, String dateFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(new Date(milliSeconds));
    }

    @Override
    public void onFileListClick( String fileUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
        startActivity(browserIntent);
    }

    @Override
    public void onRemoveBtnClick(SelectedImageModel selectedImageModel) {

    }
}
