package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.SubCategoryAdapter;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.RequestItemListType;
import com.lockdownhelp.app.Models.RequestLocationDetails;
import com.lockdownhelp.app.Models.SubCategoryModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubCategoryListActivity extends AppCompatActivity implements SubCategoryAdapter.SubCategoryEvents{


    TextView txtRequirementsTitle,txtSelect;
    Button btnSubmitRequest;
    FirebaseAuth mAuth;
    long estimatedServerTimeMs=0;
    long  offset;

    SubCategoryAdapter subCategoryAdapter;
    List<SubCategoryModel>subCategoryModels;
    RecyclerView recycler_sub_category;

    DataProcessor dataProcessor;

    String categoryIconUrl=null;

    List<RequestItemListType>requestItemListTypes;

    ProgressDialog progressDialog;
    ImageButton btnBackSubCategory;

    AVLoadingIndicatorView progress;
    RelativeLayout relFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_list);
        String requirementsTitle=getIntent().getStringExtra("requirementsTitle");
        final String categoryTitle=getIntent().getStringExtra("categoryTitle");
        String subCategoryId=getIntent().getStringExtra("subCategoryId");
        categoryIconUrl=getIntent().getStringExtra("categoryIconUrl");

        mAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        dataProcessor=DataProcessor.getInstance(this);

        btnBackSubCategory=findViewById(R.id.btnBackSubCategory);
        txtRequirementsTitle=findViewById(R.id.txtRequirementsTitle);
        txtSelect=findViewById(R.id.txtSelect);
        btnSubmitRequest=findViewById(R.id.btnSubmitRequest);
        progress=findViewById(R.id.progress);
        relFooter=findViewById(R.id.relFooter);

        requestItemListTypes=new ArrayList<>();
        requestItemListTypes.clear();

        recycler_sub_category=findViewById(R.id.recycler_sub_category);
        recycler_sub_category.setHasFixedSize(true);
        recycler_sub_category.setLayoutManager(new GridLayoutManager(this,2));

        subCategoryModels=new ArrayList<>();

        txtRequirementsTitle.setText(requirementsTitle);
        txtSelect.setText("Select "+categoryTitle);

        loadRecycler(subCategoryId);

        btnSubmitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (requestItemListTypes.size()>0){
                    //submitRequest(categoryTitle);
                if (requestItemListTypes.size()==0){
                    Toast.makeText(SubCategoryListActivity.this,"Add item to cart",Toast.LENGTH_SHORT).show();
                }else{
                    searchVolunteer(categoryTitle);
                }
                //}
            }
        });
        btnBackSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void searchVolunteer(final String categoryTitle){
        progressDialog.setMessage("Searching volunteer...");
        progressDialog.show();

        final HashMap<String,Double> hashMap=new HashMap();
        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("adminsLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict());
        //final DatabaseReference userRef=FirebaseDatabase.getInstance().getReference("Users");
        final GeoFire geoFire=new GeoFire(databaseReference);

        final DatabaseReference adminRef=FirebaseDatabase.getInstance().getReference().child("Admins");


                GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(dataProcessor.getLat()), Double.parseDouble(dataProcessor.getLng())),2000);
                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                        if (dataSnapshot.child("dept").exists()){
                            for (DataSnapshot depSnap:dataSnapshot.child("dept").getChildren()){
                                if (TextUtils.equals(depSnap.child("categoryName").getValue(String.class),categoryTitle)&&TextUtils.equals(depSnap.child("categoryCheck").getValue(String.class),"Active")){
                                    final LatLng userLatLng=new LatLng(Double.parseDouble(dataProcessor.getLat()),Double.parseDouble(dataProcessor.getLng()));
                                    final LatLng AdminLatLng=new LatLng(location.latitude,location.longitude);
                                    hashMap.put(dataSnapshot.getKey(),CalculationByDistance(userLatLng,AdminLatLng));
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onDataExited(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                    }

                    @Override
                    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        Map.Entry<String,Double>min=null;
                        for (Map.Entry<String,Double>entry:hashMap.entrySet()){
                            if (min==null||min.getValue()>entry.getValue()){
                                min=entry;
                            }
                        }
                        //Toast.makeText(SubCategoryListActivity.this,"Distance is "+min.getValue()+" for "+min.getKey(),Toast.LENGTH_SHORT).show();
                        if (min!=null){
                            getAdminDetails(categoryTitle,min.getKey(),min.getValue());
                        }else{
                            Toast.makeText(SubCategoryListActivity.this,"No Volunteers available",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(SubCategoryListActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getAdminDetails(final String categoryTitle, final String adminId, final Double distance){
        final RequestLocationDetails.AdminLocationDetails adminLocationDetails=new RequestLocationDetails.AdminLocationDetails();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("adminsLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict());
        GeoFire geoFire=new GeoFire(databaseReference);

        geoFire.getLocation(adminId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                adminLocationDetails.setLat(location.latitude);
                adminLocationDetails.setLng(location.longitude);
                getAdminAddress(categoryTitle,adminId,distance,adminLocationDetails);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(SubCategoryListActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAdminAddress(final String categoryTitle, final String adminId, final Double distance, final RequestLocationDetails.AdminLocationDetails adminLocationDetails){

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Admins").child(adminId).child("address");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("address").exists()){
                    adminLocationDetails.setAddress(dataSnapshot.child("address").getValue(String.class));
                }
                submitRequest(categoryTitle,adminId,distance,adminLocationDetails);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(SubCategoryListActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
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

    private void submitRequest(String catTitle,String adminId, Double distance, RequestLocationDetails.AdminLocationDetails adminLocationDetails){
        newData();
        progressDialog.setMessage("Submitting request...");
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Requests");


        RequestLocationDetails requestLocationDetails=new RequestLocationDetails();
        RequestLocationDetails.UserLocationDetails userLocationDetails=new RequestLocationDetails.UserLocationDetails();
        userLocationDetails.setLat(Double.parseDouble(dataProcessor.getLat()));
        userLocationDetails.setLng(Double.parseDouble(dataProcessor.getLng()));
        requestLocationDetails.setAdminLocationDetails(adminLocationDetails);
        requestLocationDetails.setUserLocationDetails(userLocationDetails);
        requestLocationDetails.setDistance(distance);

        estimatedServerTimeMs = System.currentTimeMillis() + offset;

        final String requestId=databaseReference.push().getKey();
        Request request=new Request();
        request.setUserId(mAuth.getCurrentUser().getUid());
        request.setRequestStatusMessage("Waiting for acceptance");
        request.setRequestId(requestId);
        request.setVolunteerId(adminId);
        request.setCategoryIconUrl(categoryIconUrl);
        request.setRequestViewType("List");
        request.setRequestLocationDetails(requestLocationDetails);
        request.setUserLocation(dataProcessor.getHomeLocation());
        request.setUserName(dataProcessor.getUserName());
        request.setRequestTitle(catTitle);
        request.setRequestStatusHeading("Request sent successfully");
        request.setRequestStatus("Success");
        request.setRequestTimeStamp(estimatedServerTimeMs);
        request.setRequestItemListTypeList(requestItemListTypes);

        databaseReference.child(requestId).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent=new Intent(SubCategoryListActivity.this,RequestHistoryDetailsType1.class);
                    intent.putExtra("request_id",requestId);
                    startActivity(intent);
                    finish();
                    progressDialog.dismiss();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(SubCategoryListActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

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


    private void loadRecycler(String subCategoryId ){
        progress.show();
        relFooter.setVisibility(View.GONE);
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("SubCategories").child(subCategoryId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subCategoryModels.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot subCategorySnapshot:dataSnapshot.getChildren()){
                        SubCategoryModel subCategoryModel=subCategorySnapshot.getValue(SubCategoryModel.class);
                        if (TextUtils.equals(subCategoryModel.getSubCategoryStatus(),"Active")){
                            subCategoryModels.add(subCategoryModel);
                        }
                    }
                    subCategoryAdapter=new SubCategoryAdapter(SubCategoryListActivity.this,subCategoryModels,SubCategoryListActivity.this);
                    recycler_sub_category.setAdapter(subCategoryAdapter);
                    progress.hide();
                    relFooter.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.hide();
                Toast.makeText(SubCategoryListActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onAddToCartClick(String subCategoryTitle, RelativeLayout relativeLayout, RelativeLayout linearLayout, TextView textView, double minCounter, String unit) {
        RequestItemListType requestItemListType=new RequestItemListType();
        requestItemListType.setItemName(subCategoryTitle);
        requestItemListType.setItemQuantity(minCounter);
        requestItemListType.setItemUnit(unit);

        requestItemListTypes.add(requestItemListType);
        relativeLayout.setVisibility(View.GONE);
        textView.setText(minCounter+" "+unit);
        linearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlusMinusClick(String subCategoryTitle, String typeClick, double maxCounter, double minCounter, double multipleCounter, RelativeLayout relativeLayout, RelativeLayout linearLayout, TextView textView, String unit) {
        if (TextUtils.equals(typeClick,"Minus")){
            //code for minus
            for (RequestItemListType requestItemListType:requestItemListTypes){
                if (requestItemListType!=null&& TextUtils.equals(requestItemListType.getItemName(),subCategoryTitle)){
                    if (requestItemListType.getItemQuantity()>minCounter){
                        requestItemListType.setItemQuantity(requestItemListType.getItemQuantity()-multipleCounter);
                        requestItemListTypes.set(requestItemListTypes.indexOf(requestItemListType),requestItemListType);
                        textView.setText(requestItemListType.getItemQuantity()+" "+unit);
                    }else{
                        requestItemListTypes.remove(requestItemListType);
                        relativeLayout.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);
                    }

                }
            }

        }else{
            //code for plus
            for (RequestItemListType requestItemListType:requestItemListTypes){
                if (requestItemListType!=null&& TextUtils.equals(requestItemListType.getItemName(),subCategoryTitle)){
                    if (requestItemListType.getItemQuantity()<maxCounter){
                        requestItemListType.setItemQuantity(requestItemListType.getItemQuantity()+multipleCounter);
                        requestItemListTypes.set(requestItemListTypes.indexOf(requestItemListType),requestItemListType);
                        textView.setText(requestItemListType.getItemQuantity()+" "+unit);
                    }else{
                        Toast.makeText(SubCategoryListActivity.this,"Maximum Quantity limit exceeded",Toast.LENGTH_SHORT).show();
                    }

                }
            }

        }
    }


}
