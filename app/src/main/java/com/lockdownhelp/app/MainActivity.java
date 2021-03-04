package com.lockdownhelp.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lockdownhelp.app.Adapters.CategoryAdapter;
import com.lockdownhelp.app.Adapters.RequirementsAdapter;
import com.lockdownhelp.app.Models.CategoryModel;
import com.lockdownhelp.app.Models.Request;
import com.lockdownhelp.app.Models.RequestLocationDetails;
import com.lockdownhelp.app.Models.RequirementsModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.lockdownhelp.app.Utils.RequestBottomSheetType;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RequirementsAdapter.RequirementEvents,CategoryAdapter.CategoryEvents,RequestBottomSheetType.RequestBottomSheetListener {

    TextView txtLocation,txtInfo;
    DataProcessor dataProcessor;
    RelativeLayout relLocation;
    RelativeLayout relProfile,relInfo,relHeader;
    NestedScrollView nestedScrollView;
    long estimatedServerTimeMs=0;
    long  offset;

    int foodPack=0;

    ProgressDialog progressDialog;

    TextView tRYR,txtNameInitials;
    DatabaseReference databaseReference;

    RecyclerView recycler_requirements_category_list;
    List<RequirementsModel>requirementsModels;
    RequirementsAdapter requirementsAdapter;

    FirebaseAuth mAuth;

    AVLoadingIndicatorView progressBar;

    String categoryTitle,spinnerReason,categoryIconUrl;
    RequestBottomSheetType requestBottomSheetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relHeader=findViewById(R.id.relHeader);

        txtLocation=findViewById(R.id.txtLocation);
        relLocation=findViewById(R.id.relLocation);
        relProfile=findViewById(R.id.relProfile);
        tRYR=findViewById(R.id.tRYR);

        txtInfo=findViewById(R.id.txtInfo);
        relInfo=findViewById(R.id.relInfo);
        nestedScrollView=findViewById(R.id.mainNestedScrollView);
        progressBar=findViewById(R.id.progress);
        txtNameInitials=findViewById(R.id.txtNameInitials);

        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        recycler_requirements_category_list=findViewById(R.id.recycler_requirements_category_list);
        recycler_requirements_category_list.setHasFixedSize(true);
        recycler_requirements_category_list.setLayoutManager(new LinearLayoutManager(this));

        dataProcessor=DataProcessor.getInstance(this);


        if (TextUtils.equals(dataProcessor.getUserInitials(),"None")){
            makeInitials(dataProcessor.getUserName());
        }else{
            txtNameInitials.setText(dataProcessor.getUserInitials());
        }

        txtLocation.setText(dataProcessor.getHomeLocation());
        //tRYR.setText(dataProcessor.getUserDistrict()+", "+dataProcessor.getUserState());
        relLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });

        relProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,UserProfile.class);
                startActivity(intent);
            }
        });

        progressBar.show();
        checkMyDistrict();
        newData();
        getRequirements();

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
        txtNameInitials.setText(dataProcessor.getUserInitials());

    }


    private void checkMyDistrict(){
        Query databaseReference=FirebaseDatabase.getInstance().getReference().child("SuperAdmins").orderByChild("address/district").equalTo(dataProcessor.getUserDistrict());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    nestedScrollView.setVisibility(View.VISIBLE);
                    relInfo.setVisibility(View.GONE);
                }else{
                    progressBar.hide();
                    relInfo.setVisibility(View.VISIBLE);
                    nestedScrollView.setVisibility(View.GONE);
                    txtInfo.setText("We ave not ye reached "+dataProcessor.getUserDistrict()+". We are expanding.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getRequirements(){
        requirementsModels=new ArrayList<>();
        databaseReference=FirebaseDatabase.getInstance().getReference("Requirements");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requirementsModels.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot requirementSnapshot:dataSnapshot.getChildren()){
                        RequirementsModel requirementsModel=requirementSnapshot.getValue(RequirementsModel.class);
                        requirementsModels.add(requirementsModel);
                    }
                    requirementsAdapter=new RequirementsAdapter(MainActivity.this,requirementsModels,MainActivity.this);
                    recycler_requirements_category_list.setAdapter(requirementsAdapter);
                    requirementsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void loadCategories(String categoryId, final RecyclerView categoryRecycler, final String requirementsTitle) {
        categoryRecycler.setHasFixedSize(true);
        categoryRecycler.setLayoutManager(new GridLayoutManager(this,3));

        final List<CategoryModel>categoryModels;
        categoryModels=new ArrayList<>();

        databaseReference=FirebaseDatabase.getInstance().getReference("Categories").child(categoryId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryModels.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot categorySnapshot:dataSnapshot.getChildren()){
                        CategoryModel categoryModel=categorySnapshot.getValue(CategoryModel.class);
                        categoryModels.add(categoryModel);
                    }
                    CategoryAdapter categoryAdapter =new CategoryAdapter(MainActivity.this,categoryModels,MainActivity.this,requirementsTitle);
                    categoryRecycler.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
                    relHeader.setEnabled(true);
                    relHeader.setClickable(true);
                    nestedScrollView.setVisibility(View.VISIBLE);
                }
                progressBar.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onCategoryClick(String subCategoryId,String categoryViewType,String requirementsTitle,String categoryTitle,String categoryMessage,int maxCount,String categoryIconUrl) {
        this.categoryIconUrl=categoryIconUrl;
        this.categoryTitle=categoryTitle;
        if (TextUtils.equals(dataProcessor.getUserName(),"None")){
            Intent intent=new Intent(MainActivity.this, EditProfile.class);
            startActivity(intent);
        }else{
            switch (categoryViewType){
                case "List":
                    Intent intent= new Intent(MainActivity.this,SubCategoryListActivity.class);
                    intent.putExtra("requirementsTitle",requirementsTitle);
                    intent.putExtra("categoryTitle",categoryTitle);
                    intent.putExtra("subCategoryId",subCategoryId);
                    intent.putExtra("categoryIconUrl",categoryIconUrl);
                    startActivity(intent);
                    break;
                case "ViewPager":
                case "Form":
                    searchVolunteer(categoryViewType,subCategoryId,"",categoryTitle);
                    break;
                case "BottomSheet":
                    requestBottomSheetType=new RequestBottomSheetType();
                    requestBottomSheetType.show(getSupportFragmentManager(),"bottomForm");
                    requestBottomSheetType.setCancelable(true);
                    Bundle bundle=new Bundle();
                    bundle.putString("requirementsTitle",requirementsTitle);
                    bundle.putString("categoryTitle",categoryTitle);
                    bundle.putString("subCategoryId",subCategoryId);
                    bundle.putString("categoryMessage",categoryMessage);
                    bundle.putString("bottomSheetType","BottomSheet");
                    bundle.putInt("maxCount",maxCount);
                    requestBottomSheetType.setArguments(bundle);
                    foodPack=1;
                    break;
                case "BottomSheetNone":
                    requestBottomSheetType=new RequestBottomSheetType();
                    requestBottomSheetType.show(getSupportFragmentManager(),"bottomForm");
                    requestBottomSheetType.setCancelable(true);
                    Bundle bundle1=new Bundle();
                    bundle1.putString("requirementsTitle",requirementsTitle);
                    bundle1.putString("categoryTitle",categoryTitle);
                    bundle1.putString("subCategoryId",subCategoryId);
                    bundle1.putString("categoryMessage",categoryMessage);
                    bundle1.putString("bottomSheetType","BottomSheetNone");
                    requestBottomSheetType.setArguments(bundle1);
                    break;
                case "BottomSheetSpinner":
                    requestBottomSheetType=new RequestBottomSheetType();
                    requestBottomSheetType.show(getSupportFragmentManager(),"bottomForm");
                    requestBottomSheetType.setCancelable(true);
                    Bundle bundle2=new Bundle();
                    bundle2.putString("requirementsTitle",requirementsTitle);
                    bundle2.putString("categoryTitle",categoryTitle);
                    bundle2.putString("subCategoryId",subCategoryId);
                    bundle2.putString("categoryMessage",categoryMessage);
                    bundle2.putString("bottomSheetType","BottomSheetSpinner");
                    requestBottomSheetType.setArguments(bundle2);
                    break;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        txtLocation.setText(dataProcessor.getHomeLocation());
        relHeader.setEnabled(false);
        relHeader.setClickable(false);
        if (!TextUtils.equals(dataProcessor.getUserInitials(),"None"))
        txtNameInitials.setText(dataProcessor.getUserInitials());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onChangeLocationClick() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra("openType","BottomSheet");
        startActivity(intent);
    }


    private void searchVolunteer(final String type, final String numOfPackets, final String reason, final String categoryTitle){
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
                    if (!TextUtils.equals(type,"ViewPager")&&!TextUtils.equals(type,"Form")){
                        getAdminDetails(type,numOfPackets,reason,categoryTitle,min.getKey(),min.getValue());
                    }else{
                        if (TextUtils.equals(type,"ViewPager")){
                            Intent intent1=new Intent(MainActivity.this,ViewPagerFormType.class);
                            intent1.putExtra("categoryIconUrl",categoryIconUrl);
                            intent1.putExtra("categoryTitle",categoryTitle);
                            intent1.putExtra("subCategoryId",numOfPackets);
                            startActivity(intent1);
                            progressDialog.dismiss();
                        }else if (TextUtils.equals(type,"Form")){
                            Intent intent2=new Intent(MainActivity.this,MedicineForm.class);
                            intent2.putExtra("categoryIconUrl",categoryIconUrl);
                            intent2.putExtra("categoryTitle",categoryTitle);
                            startActivity(intent2);
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"No Volunteers available",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void getAdminDetails(final String type, final String numOfPackets, final String reason, final String categoryTitle, final String adminId, final Double distance){
        final RequestLocationDetails.AdminLocationDetails adminLocationDetails=new RequestLocationDetails.AdminLocationDetails();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("adminsLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict());
        GeoFire geoFire=new GeoFire(databaseReference);

        geoFire.getLocation(adminId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                adminLocationDetails.setLat(location.latitude);
                adminLocationDetails.setLng(location.longitude);
                getAdminAddress(type,numOfPackets,reason,categoryTitle,adminId,distance,adminLocationDetails);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAdminAddress(final String type, final String numOfPackets, final String reason,final String categoryTitle, final String adminId, final Double distance, final RequestLocationDetails.AdminLocationDetails adminLocationDetails){

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Admins").child(adminId).child("address");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("address").exists()){
                    adminLocationDetails.setAddress(dataSnapshot.child("address").getValue(String.class));
                }
                submitRequest(type,numOfPackets,reason,categoryTitle,adminId,distance,adminLocationDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
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


    private void submitRequest(String type, String numOfPackets, String reason, String categoryTitle, String adminId, Double distance, RequestLocationDetails.AdminLocationDetails adminLocationDetails){
        progressDialog.setMessage("Sending "+categoryTitle+" request");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        newData();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Requests");
        final String requestId=databaseReference.push().getKey();

        RequestLocationDetails requestLocationDetails=new RequestLocationDetails();
        RequestLocationDetails.UserLocationDetails userLocationDetails=new RequestLocationDetails.UserLocationDetails();
        userLocationDetails.setLat(Double.parseDouble(dataProcessor.getLat()));
        userLocationDetails.setLng(Double.parseDouble(dataProcessor.getLng()));
        requestLocationDetails.setAdminLocationDetails(adminLocationDetails);
        requestLocationDetails.setUserLocationDetails(userLocationDetails);
        requestLocationDetails.setDistance(distance);

        estimatedServerTimeMs = System.currentTimeMillis() + offset;

        Request request=new Request();
        if (TextUtils.equals(type,"BottomSheet")){
            request.setFoodPacketCount(numOfPackets);
            request.setRequestConfirmMessage(categoryTitle+" Request for "+numOfPackets+" People");
        }else if (TextUtils.equals(type,"BottomSheetSpinner")){
            request.setRequestReason(spinnerReason);
            request.setRequestConfirmMessage("Request placed for "+reason+" to "+categoryTitle);
        }else if (TextUtils.equals(type,"BottomSheetNone")){
            if (TextUtils.equals(categoryTitle,"Fire Station")){
                request.setRequestConfirmMessage("Request placed to "+categoryTitle);
            }else{
                request.setRequestConfirmMessage("Request placed for "+categoryTitle);
            }
        }

        request.setUserId(mAuth.getCurrentUser().getUid());
        request.setRequestStatusHeading("Request Sent Successfully");
        request.setRequestStatus("Success");
        request.setRequestStatusMessage("Waiting for acceptance");
        request.setRequestId(requestId);
        request.setVolunteerId(adminId);
        request.setCategoryIconUrl(categoryIconUrl);
        request.setRequestViewType(type);
        request.setRequestLocationDetails(requestLocationDetails);
        request.setUserName(dataProcessor.getUserName());
        request.setUserName(dataProcessor.getUserName());
        request.setUserLocation(dataProcessor.getHomeLocation());
        request.setRequestTitle(categoryTitle);
        request.setRequestTimeStamp(estimatedServerTimeMs);

        databaseReference.child(requestId).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    requestBottomSheetType.dismiss();
                    Intent intent=new Intent(MainActivity.this,RequestHistoryDetailsType2.class);
                    intent.putExtra("request_id",requestId);
                    startActivity(intent);
                    progressDialog.dismiss();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    public void onSendRequestClick(String type,String numOfPackets,String reason) {
        searchVolunteer(type,numOfPackets,reason,categoryTitle);
    }

    @Override
    public void loadSpinner(String subCategoryId, final Spinner spinner) {
        final List<String>reasonList=new ArrayList<>();
        reasonList.clear();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("SubCategories").child(subCategoryId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot reasonSnapshot:dataSnapshot.getChildren()){
                    String reason=reasonSnapshot.getValue(String.class);
                    reasonList.add(reason);
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,reasonList);
                spinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerReason=reasonList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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


    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce){
            super.onBackPressed();
            this.finishAffinity();
        }else{

            this.doubleBackToExitPressedOnce=true;
            Toast.makeText(this,"Please click BACK again to exit",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            },2000);

        }

    }
}
