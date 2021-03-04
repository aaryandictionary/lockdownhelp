package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.OrderListAdapter;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.RequestItemListType;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.google.android.gms.maps.model.LatLng;
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

public class RequestHistoryDetailsType1 extends AppCompatActivity {

    TextView txtRequestMessage,txtRequestTime,txtRequestStatus,txtTotalQuantity,txtFrom,txtTo,txtDistance;
    RelativeLayout relRequestStatus,relHeader;
    RecyclerView recycler_request_items;
    Button btnCancelRequest;

    List<RequestItemListType>requestItemListTypes;
    OrderListAdapter orderListAdapter;
    String totalOrder="";
    long estimatedServerTimeMs=0;
    long  offset;
    DataProcessor dataProcessor;
    ProgressDialog progressDialog;
    ImageButton imgBtnBack;

    NestedScrollView mainNestedScrollView;
    AVLoadingIndicatorView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_history_details_type1);
        final String requestId=getIntent().getStringExtra("request_id");

        recycler_request_items=findViewById(R.id.recycler_request_items);
        txtRequestMessage=findViewById(R.id.txtRequestMessage);
        txtRequestTime=findViewById(R.id.txtRequestTime);
        txtRequestStatus=findViewById(R.id.txtRequestStatus);

        txtTotalQuantity=findViewById(R.id.txtTotalQuantity);
        relRequestStatus=findViewById(R.id.relRequestStatus);
        relHeader=findViewById(R.id.relHeader);
        btnCancelRequest=findViewById(R.id.btnCancelRequest);
        imgBtnBack=findViewById(R.id.imgBtnBack);

        txtFrom=findViewById(R.id.txtFrom);
        txtTo=findViewById(R.id.txtTo);
        txtDistance=findViewById(R.id.txtDistance);

        mainNestedScrollView=findViewById(R.id.mainNestedScrollView);
        progress=findViewById(R.id.progress);

        requestItemListTypes=new ArrayList<>();
        progressDialog=new ProgressDialog(this);

        recycler_request_items.setHasFixedSize(true);
        recycler_request_items.setLayoutManager(new LinearLayoutManager(this));

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
                    Toast.makeText(RequestHistoryDetailsType1.this,"Something went wrong",Toast.LENGTH_SHORT).show();
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
                    txtRequestStatus.setText(request.getRequestStatusMessage());
                    requestItemListTypes=request.getRequestItemListTypeList();
                    txtFrom.setText(request.getUserLocation());
                    txtTo.setText(request.getRequestLocationDetails().getAdminLocationDetails().getAddress());
                    DecimalFormat df = new DecimalFormat("####0.00");
                    txtDistance.setText(df.format(request.getRequestLocationDetails().getDistance())+" km");
                    setOrder();

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
                    }else if (TextUtils.equals(request.getRequestStatus(),"Completed")){
                        relHeader.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        relRequestStatus.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        btnCancelRequest.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        return Radius * c;
    }

    public static String getDate(long milliSeconds, String dateFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(new Date(milliSeconds));
    }

    private void setOrder(){
        HashMap<String, List<Double>> hashMap = new HashMap<String, List<Double>>();

        for (RequestItemListType requestItemListType:requestItemListTypes){
            if (!hashMap.containsKey(requestItemListType.getItemUnit())) {
                List<Double> list = new ArrayList<Double>();
                list.add(requestItemListType.getItemQuantity());

                hashMap.put(requestItemListType.getItemUnit(), list);
            } else {
                hashMap.get(requestItemListType.getItemUnit()).add(requestItemListType.getItemQuantity());
            }
        }

        for (Map.Entry<String, List<Double>> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            List<Double> value = entry.getValue();
            double sum=getSum(value);
            totalOrder= totalOrder.concat(sum+" "+key+" ");
        }

        txtTotalQuantity.setText(totalOrder);

        orderListAdapter=new OrderListAdapter(RequestHistoryDetailsType1.this,requestItemListTypes);
        recycler_request_items.setAdapter(orderListAdapter);

        progress.hide();
        mainNestedScrollView.setVisibility(View.VISIBLE);
    }

    private double getSum(List<Double>value){
        Double sum=0.0;
        for (Double o:value){
            sum=sum+o;
        }
        return sum;
    }
}
